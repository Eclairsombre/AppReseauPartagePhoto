package local.epul4a.fotoshare.dto;

import local.epul4a.fotoshare.model.Commentary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO pour la réponse d'un commentaire.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentaryResponseDto {

    private Long id;
    private String text;
    private Long photoId;
    private Long authorId;
    private String authorUsername;
    private Date createdAt;
    private boolean canDelete;
    private boolean canEdit;

    /**
     * Convertit une entité Commentary en DTO.
     */
    public static CommentaryResponseDto fromEntity(Commentary comment, String authorUsername,
                                                    boolean canDelete, boolean canEdit) {
        return CommentaryResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .photoId(comment.getPhoto_id())
                .authorId(comment.getAuthor_id())
                .authorUsername(authorUsername)
                .createdAt(comment.getCreated_at())
                .canDelete(canDelete)
                .canEdit(canEdit)
                .build();
    }
}

