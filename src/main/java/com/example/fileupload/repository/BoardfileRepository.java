package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.Boardfile;

public interface BoardfileRepository extends JpaRepository<Boardfile, Integer> {
	List<Boardfile> findByBoard_Bno(int bno);
}
