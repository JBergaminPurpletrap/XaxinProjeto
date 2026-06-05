package com.xaxin.qrcode.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xaxin.qrcode.model.Foto;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
    List<Foto> findByAlbumId(Long albumId);
}