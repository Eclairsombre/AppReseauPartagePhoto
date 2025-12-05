package local.epul4a.fotoshare.integration;
import local.epul4a.fotoshare.model.Photo;
import local.epul4a.fotoshare.model.VISIBILITY;
import local.epul4a.fotoshare.repository.PhotoRepository;
import local.epul4a.fotoshare.service.PhotoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests d'integration pour le flux d'upload de photos.
 * Teste le cycle complet : upload -> stockage disque -> entree BDD.
 */
@SpringBootTest
@ActiveProfiles("test")
class PhotoUploadIntegrationTest {
    @Autowired
    private PhotoService photoService;
    @Autowired
    private PhotoRepository photoRepository;
    @Value("${fotoshare.storage.location}")
    private String storageLocation;
    private Photo uploadedPhoto;
    private static final Long TEST_USER_ID = 1L;
    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(storageLocation));
    }
    @AfterEach
    void tearDown() {
        if (uploadedPhoto != null && uploadedPhoto.getId() != null) {
            try {
                Path filePath = Paths.get(storageLocation).resolve(uploadedPhoto.getStorage_filename());
                Files.deleteIfExists(filePath);
                if (uploadedPhoto.getThumbnail_filename() != null) {
                    Path thumbPath = Paths.get(storageLocation).resolve(uploadedPhoto.getThumbnail_filename());
                    Files.deleteIfExists(thumbPath);
                }
                photoRepository.deleteById(uploadedPhoto.getId());
            } catch (IOException e) {
            }
        }
    }
    @Test
    @DisplayName("Upload complet - Le fichier est stocke sur disque et enregistre en BDD")
    void uploadPhoto_CompleteCycle_Success() throws Exception {
        byte[] jpegBytes = createMinimalJpeg();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                jpegBytes
        );
        uploadedPhoto = photoService.uploadPhoto(
                file,
                "Photo de test",
                "Description de test",
                VISIBILITY.PRIVATE,
                TEST_USER_ID
        );
        assertNotNull(uploadedPhoto.getId(), "La photo doit avoir un ID");
        assertEquals("Photo de test", uploadedPhoto.getTitle());
        assertEquals("Description de test", uploadedPhoto.getDescription());
        assertEquals(VISIBILITY.PRIVATE, uploadedPhoto.getVisibility());
        assertEquals(TEST_USER_ID, uploadedPhoto.getOwner_id());
        assertNotNull(uploadedPhoto.getStorage_filename(), "Le nom de fichier stocke ne doit pas etre null");
        assertEquals("image/jpeg", uploadedPhoto.getContent_type());
        assertNotNull(uploadedPhoto.getCreated_at());
        Path storedFile = Paths.get(storageLocation).resolve(uploadedPhoto.getStorage_filename());
        assertTrue(Files.exists(storedFile), "Le fichier doit exister sur le disque");
        assertTrue(Files.size(storedFile) > 0, "Le fichier ne doit pas etre vide");
        Optional<Photo> foundPhoto = photoRepository.findById(uploadedPhoto.getId());
        assertTrue(foundPhoto.isPresent(), "La photo doit etre retrouvable en BDD");
        assertEquals(uploadedPhoto.getStorage_filename(), foundPhoto.get().getStorage_filename());
    }
    @Test
    @DisplayName("Upload avec titre null - Utilise le nom de fichier original")
    void uploadPhoto_WithNullTitle_UsesOriginalFilename() throws Exception {
        byte[] jpegBytes = createMinimalJpeg();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "original-name.jpg",
                "image/jpeg",
                jpegBytes
        );
        uploadedPhoto = photoService.uploadPhoto(
                file,
                null,  
                null,
                VISIBILITY.PUBLIC,
                TEST_USER_ID
        );
        assertEquals("original-name.jpg", uploadedPhoto.getTitle());
    }
    @Test
    @DisplayName("Upload avec visibilite null - Defaut PRIVATE")
    void uploadPhoto_WithNullVisibility_DefaultsToPrivate() throws Exception {
        byte[] jpegBytes = createMinimalJpeg();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                jpegBytes
        );
        uploadedPhoto = photoService.uploadPhoto(
                file,
                "Test",
                null,
                null,  
                TEST_USER_ID
        );
        assertEquals(VISIBILITY.PRIVATE, uploadedPhoto.getVisibility());
    }
    @Test
    @DisplayName("Upload fichier invalide - Leve une exception")
    void uploadPhoto_InvalidFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Ceci n'est pas une image".getBytes()
        );
        assertThrows(IllegalArgumentException.class, () -> {
            photoService.uploadPhoto(
                    file,
                    "Test",
                    null,
                    VISIBILITY.PRIVATE,
                    TEST_USER_ID
            );
        });
    }
    @Test
    @DisplayName("Upload fichier vide - Leve une exception")
    void uploadPhoto_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
        assertThrows(IllegalArgumentException.class, () -> {
            photoService.uploadPhoto(
                    file,
                    "Test",
                    null,
                    VISIBILITY.PRIVATE,
                    TEST_USER_ID
            );
        });
    }
    /**
     * Cree une image JPEG minimale valide.
     */
    private byte[] createMinimalJpeg() {
        return new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00,
            0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07,
            0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14,
            0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13,
            0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A,
            0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22,
            0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C,
            0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39,
            0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
            (byte) 0xFF, (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01,
            0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
            (byte) 0xFF, (byte) 0xC4, 0x00, 0x1F, 0x00, 0x00, 0x01,
            0x05, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x0A, 0x0B,
            (byte) 0xFF, (byte) 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00,
            0x00, 0x3F, 0x00, 0x7F, (byte) 0xFF, (byte) 0xD9
        };
    }
}
