package local.epul4a.fotoshare.dto;

import jakarta.validation.constraints.NotNull;
import local.epul4a.fotoshare.model.PERMISSION;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le partage d'une photo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareRequestDto {

    @NotNull(message = "L'ID de la photo est obligatoire")
    private Long photoId;

    private Long targetUserId;

    private String targetUsername;

    @NotNull(message = "Le niveau de permission est obligatoire")
    @Builder.Default
    private PERMISSION permission = PERMISSION.READ;
}

