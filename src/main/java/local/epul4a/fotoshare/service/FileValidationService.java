package local.epul4a.fotoshare.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de validation des fichiers uploadés.
 * Vérifie le type MIME réel via Magic Numbers et la taille du fichier.
 */
@Service
public class FileValidationService {

    // Taille maximale autorisée : 10 MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Types MIME autorisés pour les images
    private static final Map<String, byte[][]> MAGIC_NUMBERS = new HashMap<>();

    static {
        // JPEG : FF D8 FF
        MAGIC_NUMBERS.put("image/jpeg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        });

        // PNG : 89 50 4E 47 0D 0A 1A 0A
        MAGIC_NUMBERS.put("image/png", new byte[][]{
            {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A}
        });

        // GIF : 47 49 46 38 37 61 ou 47 49 46 38 39 61
        MAGIC_NUMBERS.put("image/gif", new byte[][]{
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37, (byte) 0x61},
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61}
        });

        // WebP : 52 49 46 46 xx xx xx xx 57 45 42 50
        MAGIC_NUMBERS.put("image/webp", new byte[][]{
            {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46}
        });

        // BMP : 42 4D
        MAGIC_NUMBERS.put("image/bmp", new byte[][]{
            {(byte) 0x42, (byte) 0x4D}
        });
    }

    /**
     * Valide un fichier uploadé.
     *
     * @param file Le fichier à valider
     * @return Le type MIME détecté
     * @throws IllegalArgumentException si le fichier n'est pas valide
     */
    public String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide ou null");
        }

        // Vérification de la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("La taille du fichier dépasse la limite autorisée de %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Détection du type MIME réel via Magic Numbers
        String detectedMimeType = detectMimeType(file);
        if (detectedMimeType == null) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Seuls les formats JPEG, PNG, GIF, WebP et BMP sont acceptés.");
        }

        return detectedMimeType;
    }

    /**
     * Détecte le type MIME réel du fichier en lisant ses Magic Numbers.
     *
     * @param file Le fichier à analyser
     * @return Le type MIME détecté ou null si non reconnu
     */
    private String detectMimeType(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            // Lire les premiers bytes du fichier
            byte[] header = new byte[12];
            int bytesRead = is.read(header);

            if (bytesRead < 2) {
                return null;
            }

            // Vérifier contre chaque type MIME connu
            for (Map.Entry<String, byte[][]> entry : MAGIC_NUMBERS.entrySet()) {
                String mimeType = entry.getKey();
                byte[][] signatures = entry.getValue();

                for (byte[] signature : signatures) {
                    if (bytesRead >= signature.length && matchesSignature(header, signature)) {
                        // Vérification supplémentaire pour WebP
                        if (mimeType.equals("image/webp")) {
                            if (bytesRead >= 12 && header[8] == 0x57 && header[9] == 0x45 &&
                                header[10] == 0x42 && header[11] == 0x50) {
                                return mimeType;
                            }
                        } else {
                            return mimeType;
                        }
                    }
                }
            }

            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de lire le fichier pour validation", e);
        }
    }

    /**
     * Vérifie si les bytes du header correspondent à une signature.
     */
    private boolean matchesSignature(byte[] header, byte[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (header[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retourne la taille maximale autorisée en bytes.
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Vérifie si une extension de fichier correspond au type MIME détecté.
     */
    public boolean isExtensionMatchingMimeType(String filename, String mimeType) {
        if (filename == null || mimeType == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();

        return switch (mimeType) {
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/png" -> extension.equals("png");
            case "image/gif" -> extension.equals("gif");
            case "image/webp" -> extension.equals("webp");
            case "image/bmp" -> extension.equals("bmp");
            default -> false;
        };
    }

    /**
     * Extrait l'extension d'un nom de fichier.
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Retourne l'extension appropriée pour un type MIME.
     */
    public String getExtensionForMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/bmp" -> "bmp";
            default -> "bin";
        };
    }
}

