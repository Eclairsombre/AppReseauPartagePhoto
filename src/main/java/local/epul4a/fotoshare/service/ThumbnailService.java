package local.epul4a.fotoshare.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Service pour la creation de miniatures (thumbnails) des images.
 */
@Service
public class ThumbnailService {
    @Value("${fotoshare.storage.location}")
    private String storageLocation;
    @Value("${fotoshare.thumbnail.width:300}")
    private int thumbnailWidth;
    @Value("${fotoshare.thumbnail.height:300}")
    private int thumbnailHeight;
    private static final String THUMBNAIL_SUFFIX = "_thumb";
    /**
     * Cree une miniature pour une image.
     *
     * @param originalFilename Le nom du fichier original stocke
     * @param contentType Le type MIME de l'image
     * @return Le nom du fichier miniature
     * @throws IOException Si erreur lors de la creation
     */
    public String createThumbnail(String originalFilename, String contentType) throws IOException {
        Path originalPath = Paths.get(storageLocation).resolve(originalFilename);
        if (!Files.exists(originalPath)) {
            throw new IOException("Fichier original introuvable: " + originalFilename);
        }
        String format = getFormatFromContentType(contentType);
        String thumbnailFilename = generateThumbnailFilename(originalFilename);
        Path thumbnailPath = Paths.get(storageLocation).resolve(thumbnailFilename);
        BufferedImage originalImage;
        try (InputStream is = Files.newInputStream(originalPath)) {
            originalImage = ImageIO.read(is);
        }
        if (originalImage == null) {
            throw new IOException("Impossible de lire l'image: " + originalFilename);
        }
        int[] dimensions = calculateDimensions(
            originalImage.getWidth(),
            originalImage.getHeight(),
            thumbnailWidth,
            thumbnailHeight
        );
        BufferedImage thumbnail = resizeImage(originalImage, dimensions[0], dimensions[1]);
        try (OutputStream os = Files.newOutputStream(thumbnailPath)) {
            ImageIO.write(thumbnail, format, os);
        }
        return thumbnailFilename;
    }
    /**
     * Supprime une miniature.
     *
     * @param thumbnailFilename Le nom du fichier miniature
     */
    public void deleteThumbnail(String thumbnailFilename) {
        if (thumbnailFilename == null || thumbnailFilename.isEmpty()) {
            return;
        }
        try {
            Path thumbnailPath = Paths.get(storageLocation).resolve(thumbnailFilename);
            Files.deleteIfExists(thumbnailPath);
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression de la miniature: " + e.getMessage());
        }
    }
    /**
     * Genere le nom du fichier miniature a partir du nom original.
     */
    public String generateThumbnailFilename(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            String name = originalFilename.substring(0, dotIndex);
            String extension = originalFilename.substring(dotIndex);
            return name + THUMBNAIL_SUFFIX + extension;
        }
        return originalFilename + THUMBNAIL_SUFFIX;
    }
    /**
     * Calcule les dimensions de la miniature en conservant le ratio.
     */
    private int[] calculateDimensions(int origWidth, int origHeight, int maxWidth, int maxHeight) {
        double ratio = Math.min(
            (double) maxWidth / origWidth,
            (double) maxHeight / origHeight
        );
        if (ratio >= 1.0) {
            return new int[]{origWidth, origHeight};
        }
        int newWidth = (int) (origWidth * ratio);
        int newHeight = (int) (origHeight * ratio);
        return new int[]{newWidth, newHeight};
    }
    /**
     * Redimensionne une image.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }
    /**
     * Determine le format d'image a partir du type MIME.
     */
    private String getFormatFromContentType(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/bmp" -> "bmp";
            default -> "jpg";
        };
    }
    /**
     * Verifie si une miniature existe.
     */
    public boolean thumbnailExists(String thumbnailFilename) {
        if (thumbnailFilename == null || thumbnailFilename.isEmpty()) {
            return false;
        }
        Path thumbnailPath = Paths.get(storageLocation).resolve(thumbnailFilename);
        return Files.exists(thumbnailPath);
    }
}
