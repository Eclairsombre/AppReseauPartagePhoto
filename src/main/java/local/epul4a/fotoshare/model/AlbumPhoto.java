package local.epul4a.fotoshare.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Entité de liaison entre Album et Photo.
 */
@Setter
@Getter
@Builder
@Entity
@Table(name = "album_photo")
@NoArgsConstructor
@AllArgsConstructor
public class AlbumPhoto {

    @EmbeddedId
    private AlbumPhotoId id;

    @Column(name = "added_at")
    private Date added_at;

    @PrePersist
    protected void onCreate() {
        if (added_at == null) {
            added_at = new Date();
        }
    }

    /**
     * Clé composite pour la liaison Album-Photo.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlbumPhotoId implements Serializable {
        @Column(name = "album_id")
        private Long albumId;

        @Column(name = "photo_id")
        private Long photoId;
    }
}

