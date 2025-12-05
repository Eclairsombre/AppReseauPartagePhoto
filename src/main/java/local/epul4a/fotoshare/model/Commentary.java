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
public class Commentary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "photo_id", nullable = false)
    private Long photo_id;

    @Column(name = "author_id", nullable = false)
    private Long author_id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date created_at;

    @PrePersist
    protected void onCreate() {
        if (created_at == null) {
            created_at = new Date();
        }
    }
}
