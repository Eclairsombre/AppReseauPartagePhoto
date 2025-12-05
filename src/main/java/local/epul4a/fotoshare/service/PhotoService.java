package local.epul4a.fotoshare.service;

import local.epul4a.fotoshare.model.Photo;
import local.epul4a.fotoshare.model.PERMISSION;
import local.epul4a.fotoshare.model.VISIBILITY;
import local.epul4a.fotoshare.repository.CommentaryRepository;
import local.epul4a.fotoshare.repository.PhotoRepository;
import local.epul4a.fotoshare.repository.ShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des photos.
 * Gère l'upload sécurisé, le stockage et les opérations CRUD sur les photos.
 */
@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private CommentaryRepository commentaryRepository;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PermissionService permissionService;

    /**
     * Upload une nouvelle photo avec validation complète.
     *
     * @param file Le fichier uploadé
     * @param title Le titre de la photo
     * @param description La description de la photo
     * @param visibility La visibilité de la photo
     * @param ownerId L'ID du propriétaire
     * @return La photo créée
     */
    @Transactional
    public Photo uploadPhoto(MultipartFile file, String title, String description,
                            VISIBILITY visibility, Long ownerId) {
        // Validation du fichier (type MIME via Magic Numbers + taille)
        String detectedMimeType = fileValidationService.validateFile(file);

        // Obtenir l'extension appropriée
        String extension = fileValidationService.getExtensionForMimeType(detectedMimeType);

        // Stocker le fichier avec un nom UUID
        String storageFilename = fileStorageService.store(file, detectedMimeType, extension);

        // Créer l'entité Photo
        Photo photo = Photo.builder()
                .title(title != null ? title : file.getOriginalFilename())
                .description(description)
                .original_filename(file.getOriginalFilename())
                .storage_filename(storageFilename)
                .content_type(detectedMimeType)
                .file_size(file.getSize())
                .visibility(visibility != null ? visibility : VISIBILITY.PRIVATE)
                .owner_id(ownerId)
                .created_at(new Date())
                .build();

        return photoRepository.save(photo);
    }

    /**
     * Récupère une photo par son ID avec vérification des permissions.
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur (peut être null)
     * @return La photo si accessible
     */
    public Optional<Photo> getPhoto(Long photoId, Long userId) {
        if (!permissionService.canView(photoId, userId)) {
            return Optional.empty();
        }
        return photoRepository.findById(photoId);
    }

    /**
     * Récupère le fichier d'une photo comme Resource.
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur
     * @return Le fichier comme Resource
     */
    public Resource getPhotoFile(Long photoId, Long userId) {
        if (!permissionService.canView(photoId, userId)) {
            throw new SecurityException("Accès non autorisé à cette photo");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo introuvable"));

        return fileStorageService.loadAsResource(photo.getStorage_filename());
    }

    /**
     * Met à jour les métadonnées d'une photo.
     *
     * @param photoId L'ID de la photo
     * @param title Le nouveau titre
     * @param description La nouvelle description
     * @param visibility La nouvelle visibilité
     * @param userId L'ID de l'utilisateur qui fait la demande
     * @return La photo mise à jour
     */
    @Transactional
    public Photo updatePhoto(Long photoId, String title, String description,
                            VISIBILITY visibility, Long userId) {
        if (!permissionService.canAdmin(photoId, userId)) {
            throw new SecurityException("Vous n'avez pas le droit de modifier cette photo");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo introuvable"));

        if (title != null) {
            photo.setTitle(title);
        }
        if (description != null) {
            photo.setDescription(description);
        }
        if (visibility != null) {
            photo.setVisibility(visibility);
        }

        return photoRepository.save(photo);
    }

    /**
     * Supprime une photo.
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur qui fait la demande
     */
    @Transactional
    public void deletePhoto(Long photoId, Long userId) {
        // Seul le propriétaire peut supprimer
        if (!permissionService.isOwner(photoId, userId)) {
            throw new SecurityException("Seul le propriétaire peut supprimer cette photo");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo introuvable"));

        // Supprimer le fichier physique
        fileStorageService.delete(photo.getStorage_filename());

        // Supprimer les commentaires associés
        commentaryRepository.deleteByPhotoId(photoId);

        // Supprimer les partages associés
        shareRepository.deleteByPhotoId(photoId);

        // Supprimer l'entité
        photoRepository.delete(photo);
    }

    /**
     * Récupère toutes les photos d'un propriétaire.
     *
     * @param ownerId L'ID du propriétaire
     * @return La liste des photos
     */
    public List<Photo> getPhotosByOwner(Long ownerId) {
        return photoRepository.findByOwnerId(ownerId);
    }

    /**
     * Récupère toutes les photos accessibles par un utilisateur.
     *
     * @param userId L'ID de l'utilisateur
     * @return La liste des photos accessibles
     */
    public List<Photo> getAccessiblePhotos(Long userId) {
        if (userId == null) {
            return photoRepository.findPublicPhotos();
        }
        return photoRepository.findAccessiblePhotos(userId);
    }

    /**
     * Récupère toutes les photos publiques.
     *
     * @return La liste des photos publiques
     */
    public List<Photo> getPublicPhotos() {
        return photoRepository.findPublicPhotos();
    }

    /**
     * Change la visibilité d'une photo.
     *
     * @param photoId L'ID de la photo
     * @param visibility La nouvelle visibilité
     * @param userId L'ID de l'utilisateur
     * @return La photo mise à jour
     */
    @Transactional
    public Photo changeVisibility(Long photoId, VISIBILITY visibility, Long userId) {
        if (!permissionService.isOwner(photoId, userId)) {
            throw new SecurityException("Seul le propriétaire peut changer la visibilité");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo introuvable"));

        photo.setVisibility(visibility);
        return photoRepository.save(photo);
    }

    /**
     * Récupère la permission effective d'un utilisateur sur une photo.
     */
    public PERMISSION getEffectivePermission(Long photoId, Long userId) {
        return permissionService.getEffectivePermission(photoId, userId);
    }
}
