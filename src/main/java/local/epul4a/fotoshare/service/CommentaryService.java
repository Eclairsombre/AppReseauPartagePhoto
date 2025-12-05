package local.epul4a.fotoshare.service;

import local.epul4a.fotoshare.model.Commentary;
import local.epul4a.fotoshare.repository.CommentaryRepository;
import local.epul4a.fotoshare.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Service de gestion des commentaires.
 * Vérifie les permissions avant d'autoriser les actions.
 */
@Service
public class CommentaryService {

    @Autowired
    private CommentaryRepository commentaryRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PermissionService permissionService;

    /**
     * Ajoute un commentaire à une photo.
     * L'utilisateur doit avoir au moins la permission COMMENT.
     *
     * @param photoId L'ID de la photo
     * @param text Le texte du commentaire
     * @param authorId L'ID de l'auteur
     * @return Le commentaire créé
     */
    @Transactional
    public Commentary addComment(Long photoId, String text, Long authorId) {
        // Vérifier que la photo existe
        if (!photoRepository.existsById(photoId)) {
            throw new IllegalArgumentException("Photo introuvable");
        }

        // Vérifier que l'utilisateur peut commenter
        if (!permissionService.canComment(photoId, authorId)) {
            throw new SecurityException("Vous n'avez pas le droit de commenter cette photo");
        }

        // Valider le texte
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être vide");
        }

        if (text.length() > 2000) {
            throw new IllegalArgumentException("Le commentaire ne peut pas dépasser 2000 caractères");
        }

        Commentary comment = Commentary.builder()
                .text(text.trim())
                .photo_id(photoId)
                .author_id(authorId)
                .created_at(new Date())
                .build();

        return commentaryRepository.save(comment);
    }

    /**
     * Récupère tous les commentaires d'une photo.
     * L'utilisateur doit pouvoir voir la photo.
     *
     * @param photoId L'ID de la photo
     * @param userId L'ID de l'utilisateur (peut être null)
     * @return La liste des commentaires
     */
    public List<Commentary> getCommentsForPhoto(Long photoId, Long userId) {
        // Vérifier que l'utilisateur peut voir la photo
        if (!permissionService.canView(photoId, userId)) {
            throw new SecurityException("Vous n'avez pas accès à cette photo");
        }

        return commentaryRepository.findByPhotoId(photoId);
    }

    /**
     * Supprime un commentaire.
     * Seul l'auteur du commentaire ou le propriétaire de la photo peut supprimer.
     *
     * @param commentId L'ID du commentaire
     * @param userId L'ID de l'utilisateur qui demande la suppression
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Commentary comment = commentaryRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));

        // Vérifier les permissions : auteur du commentaire ou admin de la photo
        boolean isAuthor = comment.getAuthor_id().equals(userId);
        boolean canAdminPhoto = permissionService.canAdmin(comment.getPhoto_id(), userId);

        if (!isAuthor && !canAdminPhoto) {
            throw new SecurityException("Vous n'avez pas le droit de supprimer ce commentaire");
        }

        commentaryRepository.delete(comment);
    }

    /**
     * Modifie un commentaire.
     * Seul l'auteur du commentaire peut le modifier.
     *
     * @param commentId L'ID du commentaire
     * @param newText Le nouveau texte
     * @param userId L'ID de l'utilisateur
     * @return Le commentaire modifié
     */
    @Transactional
    public Commentary updateComment(Long commentId, String newText, Long userId) {
        Commentary comment = commentaryRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));

        // Seul l'auteur peut modifier
        if (!comment.getAuthor_id().equals(userId)) {
            throw new SecurityException("Vous n'avez pas le droit de modifier ce commentaire");
        }

        // Valider le texte
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être vide");
        }

        if (newText.length() > 2000) {
            throw new IllegalArgumentException("Le commentaire ne peut pas dépasser 2000 caractères");
        }

        comment.setText(newText.trim());
        return commentaryRepository.save(comment);
    }

    /**
     * Compte le nombre de commentaires d'une photo.
     */
    public Long countComments(Long photoId) {
        return commentaryRepository.countByPhotoId(photoId);
    }

    /**
     * Supprime tous les commentaires d'une photo.
     * Utilisé lors de la suppression d'une photo.
     */
    @Transactional
    public void deleteAllCommentsForPhoto(Long photoId) {
        commentaryRepository.deleteByPhotoId(photoId);
    }
}
