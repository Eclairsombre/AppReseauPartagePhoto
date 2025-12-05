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
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

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
