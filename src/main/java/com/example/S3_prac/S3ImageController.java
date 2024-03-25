package com.example.S3_prac;

import com.example.S3_prac.dto.RequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class S3ImageController {
  private final S3FileService s3FileService;

  @GetMapping
  public String hello() {
    return "content/hello";
  }

  @PostMapping("/imageTest")
  public ResponseEntity<String> imgUpload(
    @RequestBody List<MultipartFile> files,
    @RequestPart RequestDto dto
//    @RequestParam("files") List<MultipartFile> files,
//    @ModelAttribute RequestDto dto
    ) {
    log.info("title: {}", dto.getTitle());
    log.info("content: {}", dto.getContent());

    List<String> lists = s3FileService.uploadIntoS3("/boardImg", files);

    return ResponseEntity.ok(lists.get(0));
  }
}

