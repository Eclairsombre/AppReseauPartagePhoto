package local.epul4a.fotoshare.service;
import local.epul4a.fotoshare.model.Album;
import local.epul4a.fotoshare.model.AlbumPhoto;
import local.epul4a.fotoshare.model.Photo;
import local.epul4a.fotoshare.repository.AlbumPhotoRepository;
import local.epul4a.fotoshare.repository.AlbumRepository;
import local.epul4a.fotoshare.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * Service de gestion des albums photo.
 */
@Service
public class AlbumService {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private AlbumPhotoRepository albumPhotoRepository;
    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private PermissionService permissionService;
    /**
     * Crée un nouvel album.
     */
    @Transactional
    public Album createAlbum(String name, String description, Long ownerId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'album est obligatoire");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Le nom de l'album ne peut pas dépasser 100 caractères");
        }
        Album album = Album.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .owner_id(ownerId)
                .created_at(new Date())
                .build();
        return albumRepository.save(album);
    }
    /**
     * Récupère un album par son ID.
     */
    public Optional<Album> getAlbum(Long albumId) {
        return albumRepository.findById(albumId);
    }
    /**
     * Récupère un album avec vérification des permissions.
     */
    public Optional<Album> getAlbumIfAccessible(Long albumId, Long userId) {
        Optional<Album> albumOpt = albumRepository.findById(albumId);
        if (albumOpt.isEmpty()) {
            return Optional.empty();
        }
        Album album = albumOpt.get();
        if (album.getOwner_id().equals(userId)) {
            return albumOpt;
        }
        return Optional.empty();
    }
    /**
     * Met à jour un album.
     */
    @Transactional
    public Album updateAlbum(Long albumId, String name, String description, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album introuvable"));
        if (!album.getOwner_id().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas le propriétaire de cet album");
        }
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() > 100) {
                throw new IllegalArgumentException("Le nom de l'album ne peut pas dépasser 100 caractères");
            }
            album.setName(name.trim());
        }
        if (description != null) {
            album.setDescription(description.trim());
        }
        return albumRepository.save(album);
    }
    /**
     * Supprime un album.
     */
    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album introuvable"));
        if (!album.getOwner_id().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas le propriétaire de cet album");
        }
        albumPhotoRepository.deleteByAlbumId(albumId);
        albumRepository.delete(album);
    }
    /**
     * Récupère tous les albums d'un utilisateur.
     */
    public List<Album> getAlbumsByOwner(Long ownerId) {
        return albumRepository.findByOwnerId(ownerId);
    }
    /**
     * Ajoute une photo à un album.
     */
    @Transactional
    public void addPhotoToAlbum(Long albumId, Long photoId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album introuvable"));
        if (!album.getOwner_id().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas le propriétaire de cet album");
        }
        if (!photoRepository.existsById(photoId)) {
            throw new IllegalArgumentException("Photo introuvable");
        }
        if (!permissionService.canView(photoId, userId)) {
            throw new SecurityException("Vous n'avez pas accès à cette photo");
        }
        if (albumPhotoRepository.existsByAlbumIdAndPhotoId(albumId, photoId)) {
            throw new IllegalArgumentException("Cette photo est déjà dans l'album");
        }
        AlbumPhoto.AlbumPhotoId id = new AlbumPhoto.AlbumPhotoId(albumId, photoId);
        AlbumPhoto albumPhoto = AlbumPhoto.builder()
                .id(id)
                .added_at(new Date())
                .build();
        albumPhotoRepository.save(albumPhoto);
    }
    /**
     * Retire une photo d'un album.
     */
    @Transactional
    public void removePhotoFromAlbum(Long albumId, Long photoId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album introuvable"));
        if (!album.getOwner_id().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas le propriétaire de cet album");
        }
        albumPhotoRepository.deleteByAlbumIdAndPhotoId(albumId, photoId);
    }
    /**
     * Récupère toutes les photos d'un album.
     */
    public List<Photo> getPhotosInAlbum(Long albumId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album introuvable"));
        if (!album.getOwner_id().equals(userId)) {
            throw new SecurityException("Vous n'avez pas accès à cet album");
        }
        List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByAlbumId(albumId);
        return albumPhotos.stream()
                .map(ap -> photoRepository.findById(ap.getId().getPhotoId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    /**
     * Compte le nombre de photos dans un album.
     */
    public Long countPhotosInAlbum(Long albumId) {
        return albumPhotoRepository.countByAlbumId(albumId);
    }
    /**
     * Récupère les albums contenant une photo.
     */
    public List<Album> getAlbumsContainingPhoto(Long photoId, Long userId) {
        List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByPhotoId(photoId);
        return albumPhotos.stream()
                .map(ap -> albumRepository.findById(ap.getId().getAlbumId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(album -> album.getOwner_id().equals(userId))
                .collect(Collectors.toList());
    }
    /**
     * Vérifie si l'utilisateur est propriétaire de l'album.
     */
    public boolean isOwner(Long albumId, Long userId) {
        return albumRepository.findById(albumId)
                .map(album -> album.getOwner_id().equals(userId))
                .orElse(false);
    }
}
