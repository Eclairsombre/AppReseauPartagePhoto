package local.epul4a.fotoshare.repository;

import local.epul4a.fotoshare.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT a FROM Album a WHERE a.owner_id = :ownerId ORDER BY a.created_at DESC")
    List<Album> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT a FROM Album a WHERE a.owner_id = :ownerId AND LOWER(a.name) = LOWER(:name)")
    List<Album> findByOwnerIdAndName(@Param("ownerId") Long ownerId, @Param("name") String name);
}
