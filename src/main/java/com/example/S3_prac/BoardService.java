package com.example.S3_prac;

import com.example.S3_prac.dto.RequestDto;
import com.example.S3_prac.dto.ResponseDto;
import com.example.S3_prac.entity.Board;
import com.example.S3_prac.entity.Image;
import com.example.S3_prac.repo.BoardRepository;
import com.example.S3_prac.repo.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
  private final BoardRepository boardRepository;
  private final ImageRepository imageRepository;
  private final S3FileService s3FileService;

  // Create
  @Transactional
  public ResponseDto write(
    List<MultipartFile> files,
    RequestDto dto
  ) {
    try {
      // Board Entity 생성
      Board targetBoard = Board.customBuilder()
        .title(dto.getTitle())
        .content(dto.getContent())
        .build();

      log.info("imageList: {}", targetBoard.getImageList());

      // 파일 업로드 후 URL 목록 가져오기
      if (files != null) {
        List<String> uploadedUrls = s3FileService.uploadIntoS3("/boardImg", files);

        // Image Entity 생성 및 임시 저장을 위한 리스트
        List<Image> imageToSave = new ArrayList<>();

        // Image Entity 생성 및 Board Entity와 연결
        for (String url : uploadedUrls) {
          Image targetImage = Image.builder()
            .url(url)
            .build();

          // addImage 메서드를 사용하여 Board Entity에 Image Entity 연결
          targetBoard.addImage(targetImage);

          // 임시 저장 리스트에 추가
          imageToSave.add(targetImage);

          // Entity 저장
          imageRepository.saveAll(imageToSave);
        }
      }

      // Entity 저장
      boardRepository.save(targetBoard);

      return ResponseDto.fromEntity(targetBoard);
    } catch (Exception e) {
      log.error("err: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Create a Board");
    }
  }

  // Read
  // readOne
  public ResponseDto readOne(Long boardId) {
    Board targetBoard = boardRepository.findById(boardId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Board"));

    return ResponseDto.fromEntity(targetBoard);
  }

  // Delete
  @Transactional
  public String deleteOne(Long boardId) {
    // Board 찾기
    Board targetBoard = boardRepository.findById(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No"));

    if (!targetBoard.getImageList().isEmpty()) {
      // Image 찾기
      List<Image> targetImage = imageRepository.findAllByBoard_Id(boardId);

      for (Image image : targetImage) {
        String filename = image.getUrl().substring(image.getUrl().lastIndexOf("/") + 1);

        s3FileService.deleteFile("/boardImg", filename);
        imageRepository.delete(image);
      }
    }

    // Board 삭제
    boardRepository.deleteById(boardId);

    return "done";
  }
}
