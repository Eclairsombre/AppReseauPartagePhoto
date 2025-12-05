package local.epul4a.fotoshare.repository;
import local.epul4a.fotoshare.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ShareRepository extends JpaRepository<Share, Long> {
    @Query("SELECT s FROM Share s WHERE s.photo_id = :photoId AND s.user_id = :userId")
    Optional<Share> findByPhotoIdAndUserId(@Param("photoId") Long photoId, @Param("userId") Long userId);
    @Query("SELECT s FROM Share s WHERE s.photo_id = :photoId")
    List<Share> findByPhotoId(@Param("photoId") Long photoId);
    @Query("SELECT s FROM Share s WHERE s.user_id = :userId")
    List<Share> findByUserId(@Param("userId") Long userId);
    @Modifying
    @Query("DELETE FROM Share s WHERE s.photo_id = :photoId AND s.user_id = :userId")
    void deleteByPhotoIdAndUserId(@Param("photoId") Long photoId, @Param("userId") Long userId);
    @Modifying
    @Query("DELETE FROM Share s WHERE s.photo_id = :photoId")
    void deleteByPhotoId(@Param("photoId") Long photoId);
    @Modifying
    @Query("DELETE FROM Share s WHERE s.user_id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
