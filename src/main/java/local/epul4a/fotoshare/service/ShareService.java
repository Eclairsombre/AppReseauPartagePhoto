package local.epul4a.fotoshare.service;
import local.epul4a.fotoshare.model.PERMISSION;
import local.epul4a.fotoshare.model.Share;
import local.epul4a.fotoshare.repository.ShareRepository;
import local.epul4a.fotoshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
/**
 * Service de gestion des partages de photos.
 */
@Service
public class ShareService {
    @Autowired
    private ShareRepository shareRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionService permissionService;
    /**
     * Partage une photo avec un utilisateur.
     *
     * @param photoId L'ID de la photo à partager
     * @param targetUserId L'ID de l'utilisateur destinataire
     * @param permission Le niveau de permission
     * @param requesterId L'ID de l'utilisateur qui fait le partage
     * @return Le partage créé
     */
    @Transactional
    public Share sharePhoto(Long photoId, Long targetUserId, PERMISSION permission, Long requesterId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new IllegalArgumentException("Utilisateur destinataire introuvable");
        }
        return permissionService.sharePhoto(photoId, targetUserId, permission, requesterId);
    }
    /**
     * Partage une photo avec un utilisateur par son username.
     */
    @Transactional
    public Share sharePhotoByUsername(Long photoId, String username, PERMISSION permission, Long requesterId) {
        Long targetUserId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur '" + username + "' introuvable"))
                .getId();
        return sharePhoto(photoId, targetUserId, permission, requesterId);
    }
    /**
     * Révoque un partage.
     */
    @Transactional
    public void revokeShare(Long photoId, Long targetUserId, Long requesterId) {
        permissionService.revokeShare(photoId, targetUserId, requesterId);
    }
    /**
     * Récupère tous les partages d'une photo.
     */
    public List<Share> getSharesForPhoto(Long photoId, Long requesterId) {
        return permissionService.getSharesForPhoto(photoId, requesterId);
    }
    /**
     * Récupère toutes les photos partagées avec un utilisateur.
     */
    public List<Share> getSharesForUser(Long userId) {
        return permissionService.getSharesForUser(userId);
    }
    /**
     * Met à jour le niveau de permission d'un partage.
     */
    @Transactional
    public Share updateSharePermission(Long photoId, Long targetUserId, PERMISSION newPermission, Long requesterId) {
        return permissionService.sharePhoto(photoId, targetUserId, newPermission, requesterId);
    }
}
