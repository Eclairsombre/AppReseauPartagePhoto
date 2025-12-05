package local.epul4a.fotoshare.service;
import local.epul4a.fotoshare.model.*;
import local.epul4a.fotoshare.repository.PhotoRepository;
import local.epul4a.fotoshare.repository.ShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
/**
 * Service de gestion des permissions sur les photos.
 * Gère les trois niveaux de visibilité : Propriétaire, Public, Partagé.
 */
@Service
public class PermissionService {
    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private ShareRepository shareRepository;
    /**
     * Vérifie si un utilisateur peut voir une photo.
     * Accès autorisé si :
     * - L'utilisateur est le propriétaire
     * - La photo est publique
     * - La photo a été partagée avec l'utilisateur (READ, COMMENT, ou ADMIN)
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur (peut être null pour utilisateur non connecté)
     * @return true si l'accès est autorisé
     */
    public boolean canView(Long photoId, Long userId) {
        Optional<Photo> photoOpt = photoRepository.findById(photoId);
        if (photoOpt.isEmpty()) {
            return false;
        }
        Photo photo = photoOpt.get();
        if (photo.getVisibility() == VISIBILITY.PUBLIC) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        if (photo.getOwner_id().equals(userId)) {
            return true;
        }
        return hasPermission(photoId, userId, PERMISSION.READ);
    }
    /**
     * Vérifie si un utilisateur peut commenter une photo.
     * Accès autorisé si :
     * - L'utilisateur est le propriétaire
     * - La photo a été partagée avec permission COMMENT ou ADMIN
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur peut commenter
     */
    public boolean canComment(Long photoId, Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<Photo> photoOpt = photoRepository.findById(photoId);
        if (photoOpt.isEmpty()) {
            return false;
        }
        Photo photo = photoOpt.get();
        if (photo.getOwner_id().equals(userId)) {
            return true;
        }
        return hasPermission(photoId, userId, PERMISSION.COMMENT);
    }
    /**
     * Vérifie si un utilisateur peut administrer une photo (modifier, supprimer).
     * Accès autorisé si :
     * - L'utilisateur est le propriétaire
     * - La photo a été partagée avec permission ADMIN
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur peut administrer
     */
    public boolean canAdmin(Long photoId, Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<Photo> photoOpt = photoRepository.findById(photoId);
        if (photoOpt.isEmpty()) {
            return false;
        }
        Photo photo = photoOpt.get();
        if (photo.getOwner_id().equals(userId)) {
            return true;
        }
        return hasPermission(photoId, userId, PERMISSION.ADMIN);
    }
    /**
     * Vérifie si un utilisateur est le propriétaire de la photo.
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur est propriétaire
     */
    public boolean isOwner(Long photoId, Long userId) {
        if (userId == null) {
            return false;
        }
        return photoRepository.findById(photoId)
                .map(photo -> photo.getOwner_id().equals(userId))
                .orElse(false);
    }
    /**
     * Vérifie si un utilisateur a au moins une certaine permission sur une photo.
     * Les permissions sont hiérarchiques : ADMIN > COMMENT > READ
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur
     * @param requiredPermission La permission requise
     * @return true si l'utilisateur a la permission
     */
    public boolean hasPermission(Long photoId, Long userId, PERMISSION requiredPermission) {
        Optional<Share> shareOpt = shareRepository.findByPhotoIdAndUserId(photoId, userId);
        if (shareOpt.isEmpty()) {
            return false;
        }
        PERMISSION userPermission = shareOpt.get().getPermission();
        return switch (requiredPermission) {
            case READ -> true; 
            case COMMENT -> userPermission == PERMISSION.COMMENT || userPermission == PERMISSION.ADMIN;
            case ADMIN -> userPermission == PERMISSION.ADMIN;
        };
    }
    /**
     * Partage une photo avec un utilisateur.
     * Seul le propriétaire ou un utilisateur avec permission ADMIN peut partager.
     *
     * @param photoId L'ID de la photo
     * @param targetUserId L'ID de l'utilisateur destinataire
     * @param permission Le niveau de permission à accorder
     * @param requesterId L'ID de l'utilisateur qui fait la demande
     * @return Le partage créé
     */
    @Transactional
    public Share sharePhoto(Long photoId, Long targetUserId, PERMISSION permission, Long requesterId) {
        if (!isOwner(photoId, requesterId) && !hasPermission(photoId, requesterId, PERMISSION.ADMIN)) {
            throw new SecurityException("Vous n'avez pas le droit de partager cette photo");
        }
        if (!photoRepository.existsById(photoId)) {
            throw new IllegalArgumentException("Photo introuvable");
        }
        Optional<Share> existingShare = shareRepository.findByPhotoIdAndUserId(photoId, targetUserId);
        if (existingShare.isPresent()) {
            Share share = existingShare.get();
            share.setPermission(permission);
            return shareRepository.save(share);
        } else {
            Share share = Share.builder()
                    .photo_id(photoId)
                    .user_id(targetUserId)
                    .permission(permission)
                    .created_at(new java.util.Date())
                    .build();
            return shareRepository.save(share);
        }
    }
    /**
     * Révoque le partage d'une photo avec un utilisateur.
     *
     * @param photoId L'ID de la photo
     * @param targetUserId L'ID de l'utilisateur dont on révoque l'accès
     * @param requesterId L'ID de l'utilisateur qui fait la demande
     */
    @Transactional
    public void revokeShare(Long photoId, Long targetUserId, Long requesterId) {
        if (!isOwner(photoId, requesterId) && !hasPermission(photoId, requesterId, PERMISSION.ADMIN)) {
            throw new SecurityException("Vous n'avez pas le droit de révoquer ce partage");
        }
        shareRepository.deleteByPhotoIdAndUserId(photoId, targetUserId);
    }
    /**
     * Récupère tous les partages d'une photo.
     *
     * @param photoId L'ID de la photo
     * @param requesterId L'ID de l'utilisateur qui fait la demande
     * @return La liste des partages
     */
    public List<Share> getSharesForPhoto(Long photoId, Long requesterId) {
        if (!isOwner(photoId, requesterId) && !hasPermission(photoId, requesterId, PERMISSION.ADMIN)) {
            throw new SecurityException("Vous n'avez pas le droit de voir les partages de cette photo");
        }
        return shareRepository.findByPhotoId(photoId);
    }
    /**
     * Récupère toutes les photos partagées avec un utilisateur.
     *
     * @param userId L'ID de l'utilisateur
     * @return La liste des partages
     */
    public List<Share> getSharesForUser(Long userId) {
        return shareRepository.findByUserId(userId);
    }
    /**
     * Récupère la permission effective d'un utilisateur sur une photo.
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur
     * @return La permission effective ou null si aucun accès
     */
    public PERMISSION getEffectivePermission(Long photoId, Long userId) {
        if (userId == null) {
            return photoRepository.findById(photoId)
                    .filter(p -> p.getVisibility() == VISIBILITY.PUBLIC)
                    .map(p -> PERMISSION.READ)
                    .orElse(null);
        }
        Optional<Photo> photoOpt = photoRepository.findById(photoId);
        if (photoOpt.isEmpty()) {
            return null;
        }
        Photo photo = photoOpt.get();
        if (photo.getOwner_id().equals(userId)) {
            return PERMISSION.ADMIN;
        }
        if (photo.getVisibility() == VISIBILITY.PUBLIC) {
            Optional<Share> share = shareRepository.findByPhotoIdAndUserId(photoId, userId);
            if (share.isPresent()) {
                return share.get().getPermission();
            }
            return PERMISSION.READ;
        }
        return shareRepository.findByPhotoIdAndUserId(photoId, userId)
                .map(Share::getPermission)
                .orElse(null);
    }
}
