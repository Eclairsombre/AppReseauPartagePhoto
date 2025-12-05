package local.epul4a.fotoshare.repository;
import local.epul4a.fotoshare.model.Photo;
import local.epul4a.fotoshare.model.VISIBILITY;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    @Query("SELECT p FROM Photo p WHERE p.owner_id = :ownerId")
    List<Photo> findByOwnerId(@Param("ownerId") Long ownerId);
    @Query("SELECT p FROM Photo p WHERE p.owner_id = :ownerId ORDER BY p.created_at DESC")
    Page<Photo> findByOwnerIdPaged(@Param("ownerId") Long ownerId, Pageable pageable);
    @Query("SELECT p FROM Photo p WHERE p.visibility = :visibility")
    List<Photo> findByVisibility(@Param("visibility") VISIBILITY visibility);
    @Query("SELECT p FROM Photo p WHERE p.visibility = 'PUBLIC' OR p.owner_id = :userId OR p.id IN (SELECT s.photo_id FROM Share s WHERE s.user_id = :userId)")
    List<Photo> findAccessiblePhotos(@Param("userId") Long userId);
    @Query("SELECT p FROM Photo p WHERE p.visibility = 'PUBLIC' OR p.owner_id = :userId OR p.id IN (SELECT s.photo_id FROM Share s WHERE s.user_id = :userId) ORDER BY p.created_at DESC")
    Page<Photo> findAccessiblePhotosPaged(@Param("userId") Long userId, Pageable pageable);
    @Query("SELECT p FROM Photo p WHERE p.visibility = 'PUBLIC'")
    List<Photo> findPublicPhotos();
    @Query("SELECT p FROM Photo p WHERE p.visibility = 'PUBLIC' ORDER BY p.created_at DESC")
    Page<Photo> findPublicPhotosPaged(Pageable pageable);
}
