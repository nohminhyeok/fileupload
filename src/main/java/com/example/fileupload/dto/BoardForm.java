package com.example.fileupload.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.entity.Board;

import lombok.Data;

@Data
public class BoardForm {
	private String title;
	private String pw;
	private int bno;
	private List<MultipartFile> fileList; // spring에서 input type="file"
	
	public Board toEntity() {
		Board board = new Board();
		board.setBno(this.bno); // 이거 쓰려면 bno 필드도 있어야 해
		board.setTitle(this.title);
		board.setPw(this.pw);
		return board;
	}
}
