package local.epul4a.fotoshare.service;
import local.epul4a.fotoshare.model.*;
import local.epul4a.fotoshare.repository.PhotoRepository;
import local.epul4a.fotoshare.repository.ShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * Tests unitaires pour le PermissionService.
 * Teste les scenarios d'acces aux photos selon les permissions.
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private ShareRepository shareRepository;
    @InjectMocks
    private PermissionService permissionService;
    private Photo privatePhoto;
    private Photo publicPhoto;
    private Share readShare;
    private Share commentShare;
    private Share adminShare;
    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long SHARED_USER_ID = 3L;
    private static final Long PHOTO_ID = 100L;
    @BeforeEach
    void setUp() {
        privatePhoto = Photo.builder()
                .id(PHOTO_ID)
                .title("Photo privee")
                .owner_id(OWNER_ID)
                .visibility(VISIBILITY.PRIVATE)
                .storage_filename("test.jpg")
                .content_type("image/jpeg")
                .original_filename("test.jpg")
                .created_at(new Date())
                .build();
        publicPhoto = Photo.builder()
                .id(PHOTO_ID)
                .title("Photo publique")
                .owner_id(OWNER_ID)
                .visibility(VISIBILITY.PUBLIC)
                .storage_filename("test.jpg")
                .content_type("image/jpeg")
                .original_filename("test.jpg")
                .created_at(new Date())
                .build();
        readShare = Share.builder()
                .id(1L)
                .photo_id(PHOTO_ID)
                .user_id(SHARED_USER_ID)
                .permission(PERMISSION.READ)
                .created_at(new Date())
                .build();
        commentShare = Share.builder()
                .id(2L)
                .photo_id(PHOTO_ID)
                .user_id(SHARED_USER_ID)
                .permission(PERMISSION.COMMENT)
                .created_at(new Date())
                .build();
        adminShare = Share.builder()
                .id(3L)
                .photo_id(PHOTO_ID)
                .user_id(SHARED_USER_ID)
                .permission(PERMISSION.ADMIN)
                .created_at(new Date())
                .build();
    }
    @Test
    @DisplayName("isOwner - Le proprietaire est identifie correctement")
    void isOwner_WhenUserIsOwner_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        boolean result = permissionService.isOwner(PHOTO_ID, OWNER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("isOwner - Un autre utilisateur n'est pas proprietaire")
    void isOwner_WhenUserIsNotOwner_ReturnsFalse() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        boolean result = permissionService.isOwner(PHOTO_ID, OTHER_USER_ID);
        assertFalse(result);
    }
    @Test
    @DisplayName("isOwner - Photo introuvable retourne false")
    void isOwner_WhenPhotoNotFound_ReturnsFalse() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.empty());
        boolean result = permissionService.isOwner(PHOTO_ID, OWNER_ID);
        assertFalse(result);
    }
    @Test
    @DisplayName("canView - Le proprietaire peut voir sa photo privee")
    void canView_WhenOwnerViewsPrivatePhoto_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        boolean result = permissionService.canView(PHOTO_ID, OWNER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canView - Tout le monde peut voir une photo publique")
    void canView_WhenAnyoneViewsPublicPhoto_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(publicPhoto));
        boolean result = permissionService.canView(PHOTO_ID, OTHER_USER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canView - Un utilisateur anonyme peut voir une photo publique")
    void canView_WhenAnonymousViewsPublicPhoto_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(publicPhoto));
        boolean result = permissionService.canView(PHOTO_ID, null);
        assertTrue(result);
    }
    @Test
    @DisplayName("canView - Un utilisateur non partage ne peut pas voir une photo privee")
    void canView_WhenNonSharedUserViewsPrivatePhoto_ReturnsFalse() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, OTHER_USER_ID)).thenReturn(Optional.empty());
        boolean result = permissionService.canView(PHOTO_ID, OTHER_USER_ID);
        assertFalse(result);
    }
    @Test
    @DisplayName("canView - Un utilisateur avec partage READ peut voir une photo privee")
    void canView_WhenSharedUserWithReadViewsPrivatePhoto_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(readShare));
        boolean result = permissionService.canView(PHOTO_ID, SHARED_USER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canComment - Le proprietaire peut commenter sa photo")
    void canComment_WhenOwner_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        boolean result = permissionService.canComment(PHOTO_ID, OWNER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canComment - Un utilisateur avec READ ne peut pas commenter")
    void canComment_WhenSharedUserWithReadOnly_ReturnsFalse() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(readShare));
        boolean result = permissionService.canComment(PHOTO_ID, SHARED_USER_ID);
        assertFalse(result);
    }
    @Test
    @DisplayName("canComment - Un utilisateur avec COMMENT peut commenter")
    void canComment_WhenSharedUserWithComment_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(commentShare));
        boolean result = permissionService.canComment(PHOTO_ID, SHARED_USER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canComment - Un utilisateur avec ADMIN peut commenter")
    void canComment_WhenSharedUserWithAdmin_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(adminShare));
        boolean result = permissionService.canComment(PHOTO_ID, SHARED_USER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canAdmin - Le proprietaire a les droits ADMIN")
    void canAdmin_WhenOwner_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        boolean result = permissionService.canAdmin(PHOTO_ID, OWNER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("canAdmin - Un utilisateur avec READ n'a pas les droits ADMIN")
    void canAdmin_WhenSharedUserWithRead_ReturnsFalse() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(readShare));
        boolean result = permissionService.canAdmin(PHOTO_ID, SHARED_USER_ID);
        assertFalse(result);
    }
    @Test
    @DisplayName("canAdmin - Un utilisateur avec COMMENT n'a pas les droits ADMIN")
    void canAdmin_WhenSharedUserWithComment_ReturnsFalse() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(commentShare));
        boolean result = permissionService.canAdmin(PHOTO_ID, SHARED_USER_ID);
        assertFalse(result);
    }
    @Test
    @DisplayName("canAdmin - Un utilisateur avec ADMIN a les droits ADMIN")
    void canAdmin_WhenSharedUserWithAdmin_ReturnsTrue() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(adminShare));
        boolean result = permissionService.canAdmin(PHOTO_ID, SHARED_USER_ID);
        assertTrue(result);
    }
    @Test
    @DisplayName("getEffectivePermission - Le proprietaire a ADMIN")
    void getEffectivePermission_WhenOwner_ReturnsAdmin() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        PERMISSION result = permissionService.getEffectivePermission(PHOTO_ID, OWNER_ID);
        assertEquals(PERMISSION.ADMIN, result);
    }
    @Test
    @DisplayName("getEffectivePermission - Utilisateur sans acces retourne null")
    void getEffectivePermission_WhenNoAccess_ReturnsNull() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, OTHER_USER_ID)).thenReturn(Optional.empty());
        PERMISSION result = permissionService.getEffectivePermission(PHOTO_ID, OTHER_USER_ID);
        assertNull(result);
    }
    @Test
    @DisplayName("getEffectivePermission - Retourne la permission du partage")
    void getEffectivePermission_WhenShared_ReturnsSharePermission() {
        when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(privatePhoto));
        when(shareRepository.findByPhotoIdAndUserId(PHOTO_ID, SHARED_USER_ID)).thenReturn(Optional.of(commentShare));
        PERMISSION result = permissionService.getEffectivePermission(PHOTO_ID, SHARED_USER_ID);
        assertEquals(PERMISSION.COMMENT, result);
    }
}
