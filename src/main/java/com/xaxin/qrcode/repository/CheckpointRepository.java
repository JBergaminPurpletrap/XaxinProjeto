package com.xaxin.qrcode.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xaxin.qrcode.model.Checkpoint;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {
    List<Checkpoint> findAllByOrderByOrdemAsc();
    Optional<Checkpoint> findByCodigo(String codigo);
}