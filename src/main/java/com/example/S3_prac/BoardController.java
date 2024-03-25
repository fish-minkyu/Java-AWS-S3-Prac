package com.example.S3_prac;

import com.example.S3_prac.dto.RequestDto;
import com.example.S3_prac.dto.ResponseDto;
import com.example.S3_prac.entity.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
  private final BoardService boardService;

  // Create
  @PostMapping("/write")
  public ResponseDto writeBoard(
    @RequestBody List<MultipartFile> files,
    @RequestPart RequestDto dto
    ) {
    return boardService.write(files, dto);
  }

  // Read
  // readOne
  @GetMapping("/{boardId}")
  public ResponseDto readOne(
    @PathVariable("boardId") Long boardId
  ) {
    return boardService.readOne(boardId);
  }

  // Delete
  @DeleteMapping("/{boardId}")
  public String deleteOne(
    @PathVariable("boardId") Long boardId
  ) {
    return boardService.deleteOne(boardId);
  }
}
