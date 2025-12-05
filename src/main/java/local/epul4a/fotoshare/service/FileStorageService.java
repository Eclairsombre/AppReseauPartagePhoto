package local.epul4a.fotoshare.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
/**
 * Service de stockage des fichiers photos.
 * Gère le stockage physique des fichiers hors de la racine web publique.
 */
@Service
public class FileStorageService {
    @Value("${fotoshare.storage.location:./uploads}")
    private String storageLocation;
    private Path rootLocation;
    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage", e);
        }
    }
    /**
     * Stocke un fichier avec un nom UUID pour éviter les collisions et injections.
     *
     * @param file Le fichier à stocker
     * @param mimeType Le type MIME validé du fichier
     * @param extension L'extension à utiliser
     * @return Le nom du fichier stocké (UUID + extension)
     */
    public String store(MultipartFile file, String mimeType, String extension) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Impossible de stocker un fichier vide");
        }
        String storageFilename = UUID.randomUUID().toString() + "." + extension;
        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(storageFilename))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new SecurityException("Tentative de stockage en dehors du répertoire autorisé");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageFilename;
        } catch (IOException e) {
            throw new RuntimeException("Échec du stockage du fichier", e);
        }
    }
    /**
     * Charge un fichier comme Resource.
     *
     * @param filename Le nom du fichier stocké
     * @return La ressource correspondante
     */
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            if (!file.toAbsolutePath().startsWith(rootLocation.toAbsolutePath())) {
                throw new SecurityException("Tentative d'accès en dehors du répertoire autorisé");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Impossible de lire le fichier: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier: " + filename, e);
        }
    }
    /**
     * Supprime un fichier stocké.
     *
     * @param filename Le nom du fichier à supprimer
     * @return true si le fichier a été supprimé, false sinon
     */
    public boolean delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            if (!file.toAbsolutePath().startsWith(rootLocation.toAbsolutePath())) {
                throw new SecurityException("Tentative de suppression en dehors du répertoire autorisé");
            }
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Échec de la suppression du fichier: " + filename, e);
        }
    }
    /**
     * Vérifie si un fichier existe.
     *
     * @param filename Le nom du fichier
     * @return true si le fichier existe
     */
    public boolean exists(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        if (!file.toAbsolutePath().startsWith(rootLocation.toAbsolutePath())) {
            return false;
        }
        return Files.exists(file);
    }
    /**
     * Retourne le chemin absolu du répertoire de stockage.
     */
    public Path getRootLocation() {
        return rootLocation;
    }
}
