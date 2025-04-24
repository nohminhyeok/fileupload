package com.example.fileupload.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

@Controller
public class BoardfileController {
	@Autowired BoardfileRepository boardfileRepository;
	@Autowired BoardRepository boardRepository;
	
	@GetMapping("/removeBoardfile")
	public String confirmRemoveBoardfile(@RequestParam("fno") int fno,
	                                     @RequestParam("bno") int bno,
	                                     @ModelAttribute("msg") String msg,
	                                     Model model) {
	    model.addAttribute("fno", fno);
	    model.addAttribute("bno", bno);
	    model.addAttribute("msg", msg); // FlashAttribute로 전달된 메시지 출력
	    return "removeBoardfile"; // mustache로 이동
	}

	@PostMapping("/removeBoardfile")
	public String removeBoardfile(@RequestParam("fno") int fno,
	                              @RequestParam("bno") int bno,
	                              @RequestParam("pw") String pw,
	                              RedirectAttributes rda) {

	    Boardfile boardfile = boardfileRepository.findById(fno).orElse(null);

	    Board board = boardRepository.findById(bno).orElse(null);

	    if (board.getPw() == null || !board.getPw().equals(pw)) {
	        rda.addFlashAttribute("msg", "비밀번호가 틀렸습니다.");
	        return "redirect:/removeBoardfile?fno=" + fno + "&bno=" + bno;
	    }

	    // 실제 파일 삭제
	    try {
	        File file = new File("c:/project/upload/" + boardfile.getFname() + "." + boardfile.getFext());
	        if (file.exists()) file.delete();
	    } catch (Exception e) {
	        e.printStackTrace(); // 로그로만 남기고 계속 진행
	    }

	    // DB에서 삭제
	    boardfileRepository.delete(boardfile);

	    rda.addFlashAttribute("msg", "파일이 성공적으로 삭제되었습니다.");
	    return "redirect:/boardOne?bno=" + bno;
	}

	
}
