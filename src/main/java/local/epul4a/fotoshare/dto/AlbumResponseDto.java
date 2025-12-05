package local.epul4a.fotoshare.dto;
import local.epul4a.fotoshare.model.Album;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
/**
 * DTO pour la réponse d'un album.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerUsername;
    private Date createdAt;
    private Long photoCount;
    private String coverPhotoUrl;
    /**
     * Convertit une entité Album en DTO.
     */
    public static AlbumResponseDto fromEntity(Album album, String ownerUsername, Long photoCount, String coverPhotoUrl) {
        return AlbumResponseDto.builder()
                .id(album.getId())
                .name(album.getName())
                .description(album.getDescription())
                .ownerId(album.getOwner_id())
                .ownerUsername(ownerUsername)
                .createdAt(album.getCreated_at())
                .photoCount(photoCount)
                .coverPhotoUrl(coverPhotoUrl)
                .build();
    }
}
