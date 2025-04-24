package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;

public interface BoardRepository extends JpaRepository<Board, Integer>{
	List<BoardMapping> findAllBy(); // 원하는 컬럼만 반환하기 위해
	BoardMapping findByBno(int bno);
	void deleteByBnoAndPw(int bno, String pw);
	Page<Board> findByTitleContaining(String word, Pageable pageable);
}
