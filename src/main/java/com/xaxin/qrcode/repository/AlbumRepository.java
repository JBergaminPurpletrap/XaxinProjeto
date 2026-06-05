package com.xaxin.qrcode.repository;

import com.xaxin.qrcode.model.Album;
import com.xaxin.qrcode.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findByUser(User user);
    Optional<Album> findByUserId(Long userId);
}