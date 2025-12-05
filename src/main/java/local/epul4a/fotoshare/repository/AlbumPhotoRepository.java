package local.epul4a.fotoshare.repository;

import local.epul4a.fotoshare.model.AlbumPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, AlbumPhoto.AlbumPhotoId> {

    @Query("SELECT ap FROM AlbumPhoto ap WHERE ap.id.albumId = :albumId")
    List<AlbumPhoto> findByAlbumId(@Param("albumId") Long albumId);

    @Query("SELECT ap FROM AlbumPhoto ap WHERE ap.id.photoId = :photoId")
    List<AlbumPhoto> findByPhotoId(@Param("photoId") Long photoId);

    @Query("SELECT COUNT(ap) FROM AlbumPhoto ap WHERE ap.id.albumId = :albumId")
    Long countByAlbumId(@Param("albumId") Long albumId);

    @Modifying
    @Query("DELETE FROM AlbumPhoto ap WHERE ap.id.albumId = :albumId")
    void deleteByAlbumId(@Param("albumId") Long albumId);

    @Modifying
    @Query("DELETE FROM AlbumPhoto ap WHERE ap.id.photoId = :photoId")
    void deleteByPhotoId(@Param("photoId") Long photoId);

    @Modifying
    @Query("DELETE FROM AlbumPhoto ap WHERE ap.id.albumId = :albumId AND ap.id.photoId = :photoId")
    void deleteByAlbumIdAndPhotoId(@Param("albumId") Long albumId, @Param("photoId") Long photoId);

    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END FROM AlbumPhoto ap WHERE ap.id.albumId = :albumId AND ap.id.photoId = :photoId")
    boolean existsByAlbumIdAndPhotoId(@Param("albumId") Long albumId, @Param("photoId") Long photoId);
}

