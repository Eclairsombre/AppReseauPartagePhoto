package local.epul4a.fotoshare.dto;
import local.epul4a.fotoshare.model.PERMISSION;
import local.epul4a.fotoshare.model.Share;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
/**
 * DTO pour la réponse d'un partage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponseDto {
    private Long id;
    private Long photoId;
    private Long userId;
    private String username;
    private PERMISSION permission;
    private Date createdAt;
    /**
     * Convertit une entité Share en DTO.
     */
    public static ShareResponseDto fromEntity(Share share, String username) {
        return ShareResponseDto.builder()
                .id(share.getId())
                .photoId(share.getPhoto_id())
                .userId(share.getUser_id())
                .username(username)
                .permission(share.getPermission())
                .createdAt(share.getCreated_at())
                .build();
    }
}
