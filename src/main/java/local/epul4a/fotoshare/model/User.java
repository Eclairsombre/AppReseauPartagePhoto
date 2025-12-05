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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password_hash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ROLE_USER role = ROLE_USER.USER;
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
    @Column(nullable = false, updatable = false)
    private Date created_at;
    @PrePersist
    protected void onCreate() {
        created_at = new Date();
    }
}
