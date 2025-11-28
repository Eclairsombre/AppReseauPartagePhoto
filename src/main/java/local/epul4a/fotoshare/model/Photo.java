package local.epul4a.fotoshare.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String title;
    private String description;
    private String original_filename;
    private String storage_filename;
    private String content_type;
    private VISIBILITY visibility = VISIBILITY.PRIVATE;
    private Long owner_id;
    private Date created_at;
}


