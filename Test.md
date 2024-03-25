저장하는 이미지 개수에 따라 DB를 접속해야 하는 안좋은 코드
```java
  @Transactional
  public void write(
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
      List<String> uploadedUrls = s3FileService.uploadIntoS3("/boardImg", files);

      // Image Entity 생성 및 Board Entity와 연결
      for (String url : uploadedUrls) {
        Image targetImage = Image.builder()
          .url(url)
          .build();

        // addImage 메서드를 사용하여 Board Entity에 Image Entity 연결
        targetBoard.addImage(targetImage);

        // Image Entity 저장
        imageRepository.save(targetImage);
      }

      // Entity 저장
      boardRepository.save(targetBoard);
    } catch (Exception e) {
      log.error("err: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Create a Board");
    }
  }
```