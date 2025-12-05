package local.epul4a.fotoshare.integration;
import local.epul4a.fotoshare.model.*;
import local.epul4a.fotoshare.repository.*;
import local.epul4a.fotoshare.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests d'integration pour la suppression en cascade.
 * Verifie que la suppression d'un utilisateur supprime ses photos, commentaires et albums.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CascadeDeleteIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private CommentaryRepository commentaryRepository;
    @Autowired
    private ShareRepository shareRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Value("${fotoshare.storage.location}")
    private String storageLocation;
    private User testUser;
    private User otherUser;
    private Photo userPhoto1;
    private Photo userPhoto2;
    private Commentary userComment;
    private Commentary otherUserCommentOnUserPhoto;
    private Share shareToOtherUser;
    private Album userAlbum;
    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(storageLocation));
        testUser = User.builder()
                .username("testuser")
                .email("testuser@test.com")
                .password_hash("hashedpassword")
                .role(ROLE_USER.USER)
                .enabled(true)
                .created_at(new Date())
                .build();
        testUser = userRepository.save(testUser);
        otherUser = User.builder()
                .username("otheruser")
                .email("other@test.com")
                .password_hash("hashedpassword")
                .role(ROLE_USER.USER)
                .enabled(true)
                .created_at(new Date())
                .build();
        otherUser = userRepository.save(otherUser);
        userPhoto1 = Photo.builder()
                .title("Photo 1")
                .description("Description 1")
                .original_filename("photo1.jpg")
                .storage_filename("uuid-photo1.jpg")
                .content_type("image/jpeg")
                .file_size(1000L)
                .visibility(VISIBILITY.PRIVATE)
                .owner_id(testUser.getId())
                .created_at(new Date())
                .build();
        userPhoto1 = photoRepository.save(userPhoto1);
        userPhoto2 = Photo.builder()
                .title("Photo 2")
                .description("Description 2")
                .original_filename("photo2.jpg")
                .storage_filename("uuid-photo2.jpg")
                .content_type("image/jpeg")
                .file_size(2000L)
                .visibility(VISIBILITY.PUBLIC)
                .owner_id(testUser.getId())
                .created_at(new Date())
                .build();
        userPhoto2 = photoRepository.save(userPhoto2);
        userComment = Commentary.builder()
                .text("Mon commentaire")
                .photo_id(userPhoto1.getId())
                .author_id(testUser.getId())
                .created_at(new Date())
                .build();
        userComment = commentaryRepository.save(userComment);
        otherUserCommentOnUserPhoto = Commentary.builder()
                .text("Commentaire de l'autre utilisateur")
                .photo_id(userPhoto1.getId())
                .author_id(otherUser.getId())
                .created_at(new Date())
                .build();
        otherUserCommentOnUserPhoto = commentaryRepository.save(otherUserCommentOnUserPhoto);
        shareToOtherUser = Share.builder()
                .photo_id(userPhoto1.getId())
                .user_id(otherUser.getId())
                .permission(PERMISSION.READ)
                .created_at(new Date())
                .build();
        shareToOtherUser = shareRepository.save(shareToOtherUser);
        userAlbum = Album.builder()
                .name("Mon Album")
                .description("Description de l'album")
                .owner_id(testUser.getId())
                .created_at(new Date())
                .build();
        userAlbum = albumRepository.save(userAlbum);
    }
    @Test
    @DisplayName("Suppression en cascade - Supprime toutes les photos de l'utilisateur")
    void deleteUser_DeletesAllUserPhotos() {
        assertEquals(2, photoRepository.findByOwnerId(testUser.getId()).size());
        userService.deleteUserWithCascade(testUser.getId());
        List<Photo> remainingPhotos = photoRepository.findByOwnerId(testUser.getId());
        assertTrue(remainingPhotos.isEmpty(), "Toutes les photos de l'utilisateur doivent etre supprimees");
    }
    @Test
    @DisplayName("Suppression en cascade - Supprime tous les commentaires de l'utilisateur")
    void deleteUser_DeletesAllUserComments() {
        assertEquals(1, commentaryRepository.findByAuthorId(testUser.getId()).size());
        userService.deleteUserWithCascade(testUser.getId());
        List<Commentary> remainingComments = commentaryRepository.findByAuthorId(testUser.getId());
        assertTrue(remainingComments.isEmpty(), "Tous les commentaires de l'utilisateur doivent etre supprimes");
    }
    @Test
    @DisplayName("Suppression en cascade - Supprime les commentaires sur les photos de l'utilisateur")
    void deleteUser_DeletesCommentsOnUserPhotos() {
        Long photoId = userPhoto1.getId();
        assertEquals(2, commentaryRepository.findByPhotoId(photoId).size());
        userService.deleteUserWithCascade(testUser.getId());
        assertFalse(photoRepository.findById(photoId).isPresent(), "La photo doit etre supprimee");
    }
    @Test
    @DisplayName("Suppression en cascade - Supprime tous les partages des photos de l'utilisateur")
    void deleteUser_DeletesAllSharesOfUserPhotos() {
        assertEquals(1, shareRepository.findByPhotoId(userPhoto1.getId()).size());
        userService.deleteUserWithCascade(testUser.getId());
        assertFalse(photoRepository.findById(userPhoto1.getId()).isPresent());
    }
    @Test
    @DisplayName("Suppression en cascade - Supprime tous les albums de l'utilisateur")
    void deleteUser_DeletesAllUserAlbums() {
        assertEquals(1, albumRepository.findByOwnerId(testUser.getId()).size());
        userService.deleteUserWithCascade(testUser.getId());
        List<Album> remainingAlbums = albumRepository.findByOwnerId(testUser.getId());
        assertTrue(remainingAlbums.isEmpty(), "Tous les albums de l'utilisateur doivent etre supprimes");
    }
    @Test
    @DisplayName("Suppression en cascade - Supprime l'utilisateur lui-meme")
    void deleteUser_DeletesUserRecord() {
        Long userId = testUser.getId();
        userService.deleteUserWithCascade(userId);
        assertFalse(userRepository.findById(userId).isPresent(), "L'utilisateur doit etre supprime");
    }
    @Test
    @DisplayName("Suppression en cascade - Ne supprime pas les photos des autres utilisateurs")
    void deleteUser_DoesNotDeleteOtherUsersPhotos() {
        Photo otherUserPhoto = Photo.builder()
                .title("Photo autre")
                .original_filename("other.jpg")
                .storage_filename("uuid-other.jpg")
                .content_type("image/jpeg")
                .visibility(VISIBILITY.PRIVATE)
                .owner_id(otherUser.getId())
                .created_at(new Date())
                .build();
        otherUserPhoto = photoRepository.save(otherUserPhoto);
        userService.deleteUserWithCascade(testUser.getId());
        assertTrue(photoRepository.findById(otherUserPhoto.getId()).isPresent(),
                "Les photos des autres utilisateurs ne doivent pas etre supprimees");
    }
    @Test
    @DisplayName("Suppression en cascade - Ne supprime pas les autres utilisateurs")
    void deleteUser_DoesNotDeleteOtherUsers() {
        Long otherUserId = otherUser.getId();
        userService.deleteUserWithCascade(testUser.getId());
        assertTrue(userRepository.findById(otherUserId).isPresent(),
                "Les autres utilisateurs ne doivent pas etre supprimes");
    }
    @Test
    @DisplayName("Suppression en cascade - Leve une exception si l'utilisateur n'existe pas")
    void deleteUser_ThrowsExceptionIfUserNotFound() {
        Long nonExistentUserId = 99999L;
        assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUserWithCascade(nonExistentUserId);
        });
    }
    @Test
    @DisplayName("Suppression en cascade - Scenario complet avec plusieurs entites")
    void deleteUser_CompleteScenario() {
        Long userId = testUser.getId();
        long photoCountBefore = photoRepository.findByOwnerId(userId).size();
        long commentCountBefore = commentaryRepository.findByAuthorId(userId).size();
        long albumCountBefore = albumRepository.findByOwnerId(userId).size();
        assertTrue(photoCountBefore > 0, "L'utilisateur doit avoir des photos");
        assertTrue(commentCountBefore > 0, "L'utilisateur doit avoir des commentaires");
        assertTrue(albumCountBefore > 0, "L'utilisateur doit avoir des albums");
        userService.deleteUserWithCascade(userId);
        assertEquals(0, photoRepository.findByOwnerId(userId).size());
        assertEquals(0, commentaryRepository.findByAuthorId(userId).size());
        assertEquals(0, albumRepository.findByOwnerId(userId).size());
        assertFalse(userRepository.findById(userId).isPresent());
    }
}
