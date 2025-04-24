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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
			// 업로드 파일 유효성 검증 코드 구현
			for(MultipartFile f : fileList) {
				if(f.getContentType().equals("application/octet-stream") || f.getSize() > 10*1024*1024) { // 1kbyte = 1024byte, 1mByte = 1024kbyte
					log.debug("실행 프로그램 및 10MB 이상의 파일은 업로드 불가합니다.");
					return "redirect:/addBoard";
				}
			}
			
			// 파일 업로드 진행 코드
			for(MultipartFile f : fileList) {		
				log.debug("파일 타입 : "+f.getContentType());
				log.debug("원본 이름 : "+f.getOriginalFilename());
				log.debug("파일 용량 : "+f.getSize());
				// 확장자만 추출 후 저장
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".")+1);
				log.debug("확장자 : "+ext);
				String fname = UUID.randomUUID().toString().replace("-","");
				log.debug("저장파일이름 : "+fname);
				
				File emptyFile = new File("c:/project/upload/"+fname+"."+ext);
				// f의 byte -> emptyFile로 복사
				try {
					f.transferTo(emptyFile);
				} catch (IllegalStateException | IOException e) {
					log.error("파일 저장 실패!");
					e.printStackTrace();
				}
				
				// DB(boardfile)에 파일정보 저장
				Boardfile boardfile = new Boardfile();
				boardfile.setBno(board.getBno());
				boardfile.setFext(ext);
				boardfile.setFname(fname);
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
		// 페이징
		// Sort : bno DESC
		// pageRequest 0, 10
		// page<BoardMapping>
		List<BoardMapping> list = boardRepository.findAllBy();
		model.addAttribute("list", list);
		return "boardList";
	}
	
	@GetMapping("/boardOne")
	public String boardOne(Model model, @RequestParam(value="bno") int bno) {
		BoardMapping board = boardRepository.findByBno(bno); 
		log.debug(board.toString());
		List<Boardfile> fileList = boardfileRepository.findByBno(board.getBno());
		log.debug("size : "+fileList.size());
		
		model.addAttribute("board", board);
		model.addAttribute("fileList", fileList);
		return "boardOne";
	}
	
	@GetMapping("/updateBoardTitle")
	public String updateBoardTitle(Model model, @RequestParam (value="bno") int bno) {
		Board board = boardRepository.findById(bno).orElse(null);
		model.addAttribute("Board", board);
		return "updateBoardTitle";
	}
	
	@PostMapping("/updateBoardTitle")
		public String UpdateBoardTitle2(BoardForm boardForm, RedirectAttributes rda) {
	    Board board = boardRepository.findById(boardForm.getBno()).orElse(null);
		    if (board == null) {
		        rda.addFlashAttribute("msg", "존재하지 않는 게시글입니다.");
		        return "redirect:/boardList";
		    }
	
		    // 비밀번호 검증
		    if (!board.getPw().equals(boardForm.getPw())) {
		        rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
		        return "redirect:/updateBoardTitle?bno=" + boardForm.getBno();
		    }
	
		    // 비밀번호 일치 → 제목만 수정
		    board.setTitle(boardForm.getTitle());
		    boardRepository.save(board);
		    rda.addFlashAttribute("msg", "글 수정 완료");
		    return "redirect:/boardList";
		}
	
	@GetMapping("removeBoard")
	public String removeBoard(Board board, Model model, 
								@RequestParam (value="bno") int bno) {
		model.addAttribute("bno", bno);
		boardRepository.findByBno(bno);
		boardRepository.delete(board);
		return "removeBoard";
	}
	
	@PostMapping("removeBoard")
	public String removeBoardPw(@RequestParam (value="bno") int bno
							  , @RequestParam (value="pw") String pw
							  , RedirectAttributes rda) {
	    Board board = boardRepository.findById(bno).orElse(null);
	    if (board == null) {
	        rda.addFlashAttribute("msg", "존재하지 않는 게시글입니다.");
	        return "redirect:/boardList";
	    }
	    if (!board.getPw().equals(pw)) {
	        rda.addFlashAttribute("msg", "비밀번호가 틀렸습니다.");
	        return "redirect:/removeBoard?bno=" + bno;
	    }
	    boardRepository.deleteById(bno);
	    rda.addFlashAttribute("msg", "게시글이 삭제되었습니다.");
	    return "redirect:/boardList";
	}

}

