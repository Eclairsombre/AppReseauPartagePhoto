CREATE DATABASE IF NOT EXISTS fotoshareDB;
USE fotoshareDB;

-- 1. Table users (Ajout de 'enabled')
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN', 'MODERATOR') DEFAULT 'USER',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table Photo (Ajout de 'storage_filename', 'content_type' et 'file_size')
CREATE TABLE IF NOT EXISTS photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    original_filename VARCHAR(255) NOT NULL,
    storage_filename VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(50) NOT NULL,
    file_size BIGINT,
    visibility ENUM('PRIVATE', 'PUBLIC') DEFAULT 'PRIVATE',
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_photo_owner (owner_id)
);

-- 3. Table Album
CREATE TABLE IF NOT EXISTS album (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Table de liaison Album-Photo
CREATE TABLE IF NOT EXISTS album_photo (
    album_id BIGINT NOT NULL,
    photo_id BIGINT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (album_id, photo_id),
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
    FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE,
    INDEX idx_album_photo_album (album_id),
    INDEX idx_album_photo_photo (photo_id)
);

-- 5. Table share (Ajout Contrainte d'Unicité)
CREATE TABLE IF NOT EXISTS share (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    photo_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission ENUM('READ', 'COMMENT', 'ADMIN') DEFAULT 'READ',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_photo_user (photo_id, user_id),
    INDEX idx_share_user (user_id)
);

-- 6. Table commentary
CREATE TABLE IF NOT EXISTS commentary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text TEXT NOT NULL,
    photo_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);
