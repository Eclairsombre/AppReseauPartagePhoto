package local.epul4a.fotoshare.dto;

import local.epul4a.fotoshare.model.PERMISSION;
import local.epul4a.fotoshare.model.Photo;
import local.epul4a.fotoshare.model.VISIBILITY;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO pour la réponse d'une photo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseDto {

    private Long id;
    private String title;
    private String description;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private VISIBILITY visibility;
    private Long ownerId;
    private String ownerUsername;
    private Date createdAt;
    private PERMISSION userPermission;

    /**
     * Convertit une entité Photo en DTO.
     */
    public static PhotoResponseDto fromEntity(Photo photo, String ownerUsername, PERMISSION userPermission) {
        return PhotoResponseDto.builder()
                .id(photo.getId())
                .title(photo.getTitle())
                .description(photo.getDescription())
                .originalFilename(photo.getOriginal_filename())
                .contentType(photo.getContent_type())
                .fileSize(photo.getFile_size())
                .visibility(photo.getVisibility())
                .ownerId(photo.getOwner_id())
                .ownerUsername(ownerUsername)
                .createdAt(photo.getCreated_at())
                .userPermission(userPermission)
                .build();
    }
}

