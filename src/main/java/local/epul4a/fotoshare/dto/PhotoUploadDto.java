package local.epul4a.fotoshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import local.epul4a.fotoshare.model.VISIBILITY;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO pour l'upload d'une photo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadDto {

    @NotNull(message = "Le fichier est obligatoire")
    private MultipartFile file;

    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @Builder.Default
    private VISIBILITY visibility = VISIBILITY.PRIVATE;
}

