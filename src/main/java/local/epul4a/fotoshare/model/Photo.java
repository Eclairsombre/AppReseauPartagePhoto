package local.epul4a.fotoshare.model;

import jakarta.persistence.*;
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

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_filename", nullable = false)
    private String original_filename;

    @Column(name = "storage_filename", nullable = false, unique = true)
    private String storage_filename;

    @Column(name = "content_type", nullable = false, length = 50)
    private String content_type;

    @Column(name = "file_size")
    private Long file_size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VISIBILITY visibility = VISIBILITY.PRIVATE;

    @Column(name = "owner_id", nullable = false)
    private Long owner_id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date created_at;

    @PrePersist
    protected void onCreate() {
        if (created_at == null) {
            created_at = new Date();
        }
    }
}


