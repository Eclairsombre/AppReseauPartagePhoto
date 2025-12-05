package local.epul4a.fotoshare.service;
import local.epul4a.fotoshare.dto.AlbumResponseDto;
import local.epul4a.fotoshare.dto.CommentaryResponseDto;
import local.epul4a.fotoshare.dto.PhotoResponseDto;
import local.epul4a.fotoshare.dto.ShareResponseDto;
import local.epul4a.fotoshare.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests unitaires pour les mappers (conversion Entite <-> DTO).
 */
class DtoMapperTest {
    @Nested
    @DisplayName("PhotoResponseDto Mapper Tests")
    class PhotoResponseDtoMapperTest {
        @Test
        @DisplayName("fromEntity - Convertit correctement une Photo en DTO")
        void fromEntity_ConvertsAllFields() {
            Date createdAt = new Date();
            Photo photo = Photo.builder()
                    .id(1L)
                    .title("Photo de vacances")
                    .description("Une belle photo de plage")
                    .original_filename("vacances.jpg")
                    .storage_filename("uuid-12345.jpg")
                    .content_type("image/jpeg")
                    .file_size(1024000L)
                    .visibility(VISIBILITY.PUBLIC)
                    .owner_id(10L)
                    .created_at(createdAt)
                    .build();
            String ownerUsername = "john_doe";
            PERMISSION permission = PERMISSION.READ;
            PhotoResponseDto dto = PhotoResponseDto.fromEntity(photo, ownerUsername, permission);
            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("Photo de vacances", dto.getTitle());
            assertEquals("Une belle photo de plage", dto.getDescription());
            assertEquals("vacances.jpg", dto.getOriginalFilename());
            assertEquals("image/jpeg", dto.getContentType());
            assertEquals(1024000L, dto.getFileSize());
            assertEquals(VISIBILITY.PUBLIC, dto.getVisibility());
            assertEquals(10L, dto.getOwnerId());
            assertEquals("john_doe", dto.getOwnerUsername());
            assertEquals(createdAt, dto.getCreatedAt());
            assertEquals(PERMISSION.READ, dto.getUserPermission());
        }
        @Test
        @DisplayName("fromEntity - Gere les valeurs null")
        void fromEntity_HandlesNullValues() {
            Photo photo = Photo.builder()
                    .id(1L)
                    .title(null)
                    .description(null)
                    .original_filename("test.jpg")
                    .storage_filename("uuid.jpg")
                    .content_type("image/jpeg")
                    .file_size(null)
                    .visibility(VISIBILITY.PRIVATE)
                    .owner_id(1L)
                    .created_at(null)
                    .build();
            PhotoResponseDto dto = PhotoResponseDto.fromEntity(photo, null, null);
            assertNotNull(dto);
            assertNull(dto.getTitle());
            assertNull(dto.getDescription());
            assertNull(dto.getFileSize());
            assertNull(dto.getOwnerUsername());
            assertNull(dto.getUserPermission());
            assertNull(dto.getCreatedAt());
        }
        @Test
        @DisplayName("fromEntity - Conserve la visibilite PRIVATE")
        void fromEntity_PreservesPrivateVisibility() {
            Photo photo = Photo.builder()
                    .id(1L)
                    .original_filename("test.jpg")
                    .storage_filename("uuid.jpg")
                    .content_type("image/jpeg")
                    .visibility(VISIBILITY.PRIVATE)
                    .owner_id(1L)
                    .build();
            PhotoResponseDto dto = PhotoResponseDto.fromEntity(photo, "user", PERMISSION.ADMIN);
            assertEquals(VISIBILITY.PRIVATE, dto.getVisibility());
        }
        @Test
        @DisplayName("fromEntity - Conserve tous les types de permission")
        void fromEntity_PreservesAllPermissionTypes() {
            Photo photo = Photo.builder()
                    .id(1L)
                    .original_filename("test.jpg")
                    .storage_filename("uuid.jpg")
                    .content_type("image/jpeg")
                    .visibility(VISIBILITY.PUBLIC)
                    .owner_id(1L)
                    .build();
            PhotoResponseDto dtoRead = PhotoResponseDto.fromEntity(photo, "user", PERMISSION.READ);
            assertEquals(PERMISSION.READ, dtoRead.getUserPermission());
            PhotoResponseDto dtoComment = PhotoResponseDto.fromEntity(photo, "user", PERMISSION.COMMENT);
            assertEquals(PERMISSION.COMMENT, dtoComment.getUserPermission());
            PhotoResponseDto dtoAdmin = PhotoResponseDto.fromEntity(photo, "user", PERMISSION.ADMIN);
            assertEquals(PERMISSION.ADMIN, dtoAdmin.getUserPermission());
        }
    }
    @Nested
    @DisplayName("CommentaryResponseDto Mapper Tests")
    class CommentaryResponseDtoMapperTest {
        @Test
        @DisplayName("fromEntity - Convertit correctement un Commentary en DTO")
        void fromEntity_ConvertsAllFields() {
            Date createdAt = new Date();
            Commentary comment = Commentary.builder()
                    .id(1L)
                    .text("Super photo !")
                    .photo_id(100L)
                    .author_id(50L)
                    .created_at(createdAt)
                    .build();
            String authorUsername = "jane_doe";
            boolean canDelete = true;
            boolean canEdit = false;
            CommentaryResponseDto dto = CommentaryResponseDto.fromEntity(comment, authorUsername, canDelete, canEdit);
            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("Super photo !", dto.getText());
            assertEquals(100L, dto.getPhotoId());
            assertEquals(50L, dto.getAuthorId());
            assertEquals("jane_doe", dto.getAuthorUsername());
            assertEquals(createdAt, dto.getCreatedAt());
            assertTrue(dto.isCanDelete());
            assertFalse(dto.isCanEdit());
        }
        @Test
        @DisplayName("fromEntity - Gere les permissions d'edition et suppression")
        void fromEntity_HandlesPermissions() {
            Commentary comment = Commentary.builder()
                    .id(1L)
                    .text("Test")
                    .photo_id(1L)
                    .author_id(1L)
                    .created_at(new Date())
                    .build();
            CommentaryResponseDto dto1 = CommentaryResponseDto.fromEntity(comment, "user", true, true);
            assertTrue(dto1.isCanDelete());
            assertTrue(dto1.isCanEdit());
            CommentaryResponseDto dto2 = CommentaryResponseDto.fromEntity(comment, "user", false, false);
            assertFalse(dto2.isCanDelete());
            assertFalse(dto2.isCanEdit());
        }
        @Test
        @DisplayName("fromEntity - Gere le texte long")
        void fromEntity_HandlesLongText() {
            String longText = "A".repeat(2000);
            Commentary comment = Commentary.builder()
                    .id(1L)
                    .text(longText)
                    .photo_id(1L)
                    .author_id(1L)
                    .created_at(new Date())
                    .build();
            CommentaryResponseDto dto = CommentaryResponseDto.fromEntity(comment, "user", false, false);
            assertEquals(2000, dto.getText().length());
            assertEquals(longText, dto.getText());
        }
    }
    @Nested
    @DisplayName("ShareResponseDto Mapper Tests")
    class ShareResponseDtoMapperTest {
        @Test
        @DisplayName("fromEntity - Convertit correctement un Share en DTO")
        void fromEntity_ConvertsAllFields() {
            Date createdAt = new Date();
            Share share = Share.builder()
                    .id(1L)
                    .photo_id(100L)
                    .user_id(50L)
                    .permission(PERMISSION.COMMENT)
                    .created_at(createdAt)
                    .build();
            String username = "shared_user";
            ShareResponseDto dto = ShareResponseDto.fromEntity(share, username);
            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals(100L, dto.getPhotoId());
            assertEquals(50L, dto.getUserId());
            assertEquals("shared_user", dto.getUsername());
            assertEquals(PERMISSION.COMMENT, dto.getPermission());
            assertEquals(createdAt, dto.getCreatedAt());
        }
        @Test
        @DisplayName("fromEntity - Conserve tous les types de permission")
        void fromEntity_PreservesAllPermissionTypes() {
            Share share = Share.builder()
                    .id(1L)
                    .photo_id(1L)
                    .user_id(1L)
                    .created_at(new Date())
                    .build();
            share.setPermission(PERMISSION.READ);
            ShareResponseDto dtoRead = ShareResponseDto.fromEntity(share, "user");
            assertEquals(PERMISSION.READ, dtoRead.getPermission());
            share.setPermission(PERMISSION.COMMENT);
            ShareResponseDto dtoComment = ShareResponseDto.fromEntity(share, "user");
            assertEquals(PERMISSION.COMMENT, dtoComment.getPermission());
            share.setPermission(PERMISSION.ADMIN);
            ShareResponseDto dtoAdmin = ShareResponseDto.fromEntity(share, "user");
            assertEquals(PERMISSION.ADMIN, dtoAdmin.getPermission());
        }
        @Test
        @DisplayName("fromEntity - Gere le username null")
        void fromEntity_HandlesNullUsername() {
            Share share = Share.builder()
                    .id(1L)
                    .photo_id(1L)
                    .user_id(1L)
                    .permission(PERMISSION.READ)
                    .created_at(new Date())
                    .build();
            ShareResponseDto dto = ShareResponseDto.fromEntity(share, null);
            assertNull(dto.getUsername());
        }
    }
    @Nested
    @DisplayName("AlbumResponseDto Mapper Tests")
    class AlbumResponseDtoMapperTest {
        @Test
        @DisplayName("fromEntity - Convertit correctement un Album en DTO")
        void fromEntity_ConvertsAllFields() {
            Date createdAt = new Date();
            Album album = Album.builder()
                    .id(1L)
                    .name("Vacances 2024")
                    .description("Photos de nos vacances en Espagne")
                    .owner_id(10L)
                    .created_at(createdAt)
                    .build();
            String ownerUsername = "john_doe";
            Long photoCount = 42L;
            String coverPhotoUrl = "/photos/1/thumbnail";
            AlbumResponseDto dto = AlbumResponseDto.fromEntity(album, ownerUsername, photoCount, coverPhotoUrl);
            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("Vacances 2024", dto.getName());
            assertEquals("Photos de nos vacances en Espagne", dto.getDescription());
            assertEquals(10L, dto.getOwnerId());
            assertEquals("john_doe", dto.getOwnerUsername());
            assertEquals(createdAt, dto.getCreatedAt());
            assertEquals(42L, dto.getPhotoCount());
            assertEquals("/photos/1/thumbnail", dto.getCoverPhotoUrl());
        }
        @Test
        @DisplayName("fromEntity - Gere les valeurs null")
        void fromEntity_HandlesNullValues() {
            Album album = Album.builder()
                    .id(1L)
                    .name("Album vide")
                    .description(null)
                    .owner_id(1L)
                    .created_at(null)
                    .build();
            AlbumResponseDto dto = AlbumResponseDto.fromEntity(album, null, 0L, null);
            assertNotNull(dto);
            assertNull(dto.getDescription());
            assertNull(dto.getOwnerUsername());
            assertNull(dto.getCoverPhotoUrl());
            assertNull(dto.getCreatedAt());
            assertEquals(0L, dto.getPhotoCount());
        }
        @Test
        @DisplayName("fromEntity - Gere un album sans photos")
        void fromEntity_HandlesEmptyAlbum() {
            Album album = Album.builder()
                    .id(1L)
                    .name("Album vide")
                    .owner_id(1L)
                    .created_at(new Date())
                    .build();
            AlbumResponseDto dto = AlbumResponseDto.fromEntity(album, "user", 0L, null);
            assertEquals(0L, dto.getPhotoCount());
            assertNull(dto.getCoverPhotoUrl());
        }
    }
}
