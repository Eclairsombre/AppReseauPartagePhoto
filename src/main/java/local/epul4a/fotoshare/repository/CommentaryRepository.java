package local.epul4a.fotoshare.repository;

import local.epul4a.fotoshare.model.Commentary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentaryRepository extends JpaRepository<Commentary, Long> {

    @Query("SELECT c FROM Commentary c WHERE c.photo_id = :photoId ORDER BY c.created_at DESC")
    List<Commentary> findByPhotoId(@Param("photoId") Long photoId);

    @Query("SELECT c FROM Commentary c WHERE c.author_id = :authorId ORDER BY c.created_at DESC")
    List<Commentary> findByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(c) FROM Commentary c WHERE c.photo_id = :photoId")
    Long countByPhotoId(@Param("photoId") Long photoId);

    @Modifying
    @Query("DELETE FROM Commentary c WHERE c.photo_id = :photoId")
    void deleteByPhotoId(@Param("photoId") Long photoId);
}
