package com.xaxin.qrcode.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xaxin.qrcode.model.QrNumber;

@Repository
public interface QrNumberRepository extends JpaRepository<QrNumber, Long> {
	List<QrNumber> findAllByUserUsername(String username);
}