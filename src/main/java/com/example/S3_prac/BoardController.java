package com.example.S3_prac;

import com.example.S3_prac.dto.RequestDto;
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

  // Create
  @PostMapping("/write")
  public void writeBoard(
    @RequestBody List<MultipartFile> files,
    @RequestPart RequestDto dto
    ) {
    try {

    } catch (Exception e) {
      log.error("err: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Create a Board");
    }
  }

  // Read

  // Delete
}
