package com.example.fileupload.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.FileuploadApplication;
import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {

    private final FileuploadApplication fileuploadApplication;

    BoardController(FileuploadApplication fileuploadApplication) {
        this.fileuploadApplication = fileuploadApplication;
    }
	// 입력폼
	@GetMapping("/addBoard")
	public String addBoard() {
		return "addBoard";
	}
	// 입력액션
	@PostMapping("/addBoard")
	public String addBoard(BoardForm boardForm) {
		log.debug(boardForm.toString());
		// 이슈 : 파일을 첨부하지 않아도 fileSize는 1이다
		log.debug("MultipartFile.size : "+boardForm.getFileList().size());
		
		Board board = new Board();
		board.setTitle(boardForm.getTitle());
		board.setPw(boardForm.getPw());
		boardRepository.save(board); // board에 저장
		int bno = board.getBno(); // board에 저장 후 bno 변경되었는지
		log.debug("bno : "+bno);
		
		// 파일 분리
		List<MultipartFile> fileList = boardForm.getFileList();
		long firstFileSize = fileList.get(0).getSize();
		log.debug("firstFileSize : " +firstFileSize);
		
		if(firstFileSize > 0) { // 첫번째 파일사이즈가 0이상이다 -> 첨부된 파일이 있다.
			for(MultipartFile f : fileList) {		
				log.debug("파일 타입 : "+f.getContentType());
				log.debug("원본 이름 : "+f.getOriginalFilename());
				log.debug("파일 용량 : "+f.getSize());
				// 확장자만 추출 후 저장
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".")+1);
				log.debug("확장자 : "+ext);
				String saveName = UUID.randomUUID().toString().replace("-","");
				log.debug("저장파일이름 : "+saveName);
				
				File emptyFile = new File("c:/project/upload/"+saveName+"."+ext);
				// f의 byte -> emptyFile로 복사
				try {
					f.transferTo(emptyFile);
				} catch (IllegalStateException | IOException e) {
					log.error("파일 저장 실패!");
					e.printStackTrace();
				}
				
				// DB(boardfile)에 파일정보 저장
				Boardfile boardfile = new Boardfile();
				boardfile.setBoard(board);
				boardfile.setFext(ext);
				boardfile.setFname(saveName);
				boardfile.setForiginname(f.getOriginalFilename());
				boardfile.setFsize(f.getSize());
				boardfile.setFtype(f.getContentType());
				boardfileRepository.save(boardfile);
				}
		}
		
		return "redirect:/boardList";
	}
	
	@Autowired BoardRepository boardRepository;
	@Autowired BoardfileRepository boardfileRepository;
	
	@GetMapping({"/","/boardList"})
	public String boardList(Model model) {
	List<BoardMapping> list = boardRepository.findAllBy();
		model.addAttribute("list", list);
		return "boardList";
	}
	
	@GetMapping("/boardOne")
	public String boardOne(Model model, @RequestParam(value="bno") int bno) {
		BoardMapping board = boardRepository.findByBno(bno); 
		log.debug(board.toString());
		List<Boardfile> fileList = boardfileRepository.findByBoard_Bno(board.getBno());
		log.debug("size : "+fileList.size());
		
		model.addAttribute("board", board);
		model.addAttribute("fileList", fileList);
		return "boardOne";
	}
}
