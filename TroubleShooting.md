



# Trouble Shooting


<details>
<summary><strong>1. Image Entity를 저장할 때, DB에 여러번 접근하여 성능 저하</strong></summary>

## 문제

아래 코드대로 저장을 하게 된다면, 저장할 이미지의 개수만큼 DB에 접근하므로 그에 따른 성능 저하 발생
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

## 해결

`saveAll(Image image)`로 한꺼번에 저장함으로서  
저장할 이미지의 개수에 상관없이 DB를 한번만 접근하도록 하였다.
```java
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
```

</details>


<details>
<summary><strong>2. Board의 imageList - NullPointerException 발생</strong></summary>

## 문제.

아래 코드와 같이 S3 버킷의 저장을 시도를 했으나 자꾸 Board Entity의 imageList가 null이므로
NullPointerException 계속 발생하였다.

`해당 에러 메시지`
```java
Cannot invoke "java.util.List.add(Object)" because "this.imageList" is null
```

추측되는 원인은 2가지였다.
1. Board 클래스 내에서 imageList 필드가 초기화 되지 않으므로 null 발생
  
2. 클래스의 다른 생성자를 통해 객체가 생성되는 경우
@NoArgsConstructor 어노테이션을 사용하여 기본 생성자를 생성하고 있다.  
이 기본 생성자를 통해 객체가 생성될 때, 명시적으로 imageList를 초기화하는 코드가 없다면  
imageList는 null일 수 있으나 Board Entity에서 초기화를 잘 해주고 있으므로 원인은 아니다.



```java
@Transactional
public void write(List<MultipartFile> files, RequestDto dto) {
  try {
    // Board 엔티티 생성
    Board targetBoard = Board.builder()
      .title(dto.getTitle())
      .content(dto.getContent())
      .build();

    // 파일 업로드 후 URL 목록 가져오기
    List<String> uploadedUrls = uploadIntoS3("/boardImages", files);

    // Image 엔티티 생성 및 Board 엔티티와 연결
    for (String url : uploadedUrls) {
      Image image = Image.builder()
          .url(url)
          .build();
      
      targetBoard.addImage(image); // NullPointerException 발생
    }

    // ...
  } catch (Exception e) {
    log.error("err: {}", e.getMessage());
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Create a Board");
  }
}
```

## 해결.

Board Entity에 CustomBuilder 메소드를 만듦으로서 문제를 해결하였다.  
[Board Entity](/src/main/java/com/example/S3_prac/entity/Board.java)
```java
  // imageList NullPointException 발생으로 인한 CustomBuilder
  public static Board.BoardBuilder customBuilder() {
    return builder()
      .imageList(new ArrayList<>());
  }
```

</details>

<details>
<summary><strong>3. 저장할 이미지가 없다면 Error 발생</strong></summary>

## 문제.
BoardService의 wirte 메소드 실행 시, 이미자가 없을 때 분기처리를 해주지 않아 에러가 발생했다.

## 해결.

if문을 통해 이미지가 있을 때와 없을 때의 분기처리를 잘 진행했다.
이 때, files는 null이므로 빈 리스트인지 확인하는 isEmpty() 메소드는 사용하면 안된다.
[BoardService - write()](/src/main/java/com/example/S3_prac/BoardService.java)
```java
  @Transactional
  public ResponseDto write(
    List<MultipartFile> files,
    RequestDto dto
  ) {
    try {
      // Board Entity 생성
      // ...

      log.info("imageList: {}", targetBoard.getImageList());

      // 파일 업로드 후 URL 목록 가져오기
      if (files != null) {
       // ...
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
```

</details>

<details>
<summary><strong>4. S3 버킷 삭제가 안됨</strong></summary>

## 문제.
이미지 삭제 로직 구현 시, S3 버킷 이미지가 삭제가 되지 않았다.
```java
  @Transactional
  public void deleteFile(String folder, String filename) {
    amazonS3.deleteObject(bucket + folder, filename);
  }
```

## 해결.
알고보니 매개변수를 잘못 넣은 것이었다.
나는 아래와 같이 매개변수를 넣고 있었다.
```java
https://s3.ap-northeast-2.amazonaws.com/java-test-s3/boardImg/d17515d2-d7e1-4d6d-80a7-cd9b3c519506.png
```

하지만, 보는 바와 같이 amazonS3.deleteObject()의 매개변수에는 다음과 같이 들어가야 한다.
```java
boardImg/d17515d2-d7e1-4d6d-80a7-cd9b3c519506.png
```

즉, filename이 매개변수로 들어가야 삭제할 수 있었던 것이다.
</details>

<details>
<summary><strong>5. Image객체와 Board 객체 사이에 순환 참조 발생</strong></summary>

## 문제.

write() 메서드를 사용하면 반환값이 순환참조가 발생하여    
데이터 크기가 매우 큰 상태로 반환되는 문제가 발생했다.

`순환참조 발생`
```java
{
    "id": 1,
    "title": "test",
    "content": "test",
    "imageList": [
    {
    "id": 1,
    "url": "https://s3.ap-northeast-2.amazonaws.com/java-test-s3/boardImg/157633bf-7884-48ce-81dd-3d03ce74c32c.png",
    "board": {
    "id": 1,
    "title": "test",
    "content": "test",
    "imageList": [
    {
    "id": 1,
    "url": "https://s3.ap-northeast-2.amazonaws.com/java-test-s3/boardImg/157633bf-7884-48ce-81dd-3d03ce74c32c.png",
    "board": {
    "id": 1,
    "title": "test",
    "content": "test",
    "imageList": [
    {
    "id": 1,
    "url": "https://s3.ap-northeast-2.amazonaws.com/java-test-s3/boardImg/
```

그 이유는, Image 객체와 Board 객체 사이에 순환 참조가 발생하기 때문이다.  
Image 객체 내부에 Board 객체가  있고, Board 객체 내부에 다시 Image 객체가 있는 구조로  
되어 있어서 한 객체를 직렬화할 때 서로를 끊임없이 참조하게 되어 결과적으로 무한히 큰 JSON이 생성된다.


## 해결.

1. `@JsonManagedReference`와 `@JsonBackReference` 사용하기

Jackson 라이브러리를 사용하고 있다면, 순환 참조를 처리하기 위해 이 어노테이션을 사용할 수 있다.  
부모 엔티티에 `@JsonManagedReference`를,  
자식 엔티티에 `@JsonBackReference`를 붙여 순환 참조 문제를 해결할 수 있다.

```java
public class Board {
    @JsonManagedReference
    private List<Image> imageList;
    // 나머지 코드
}
```
```java
public class Image {
    @JsonBackReference
    private Board board;
    // 나머지 코드
}
```

2. DTO 사용하기

엔티티 클래스(Board, Image) 대신에 순환 참조를 포함하지 않는 DTO 클래스를 만들어서 사용하는 방법이다.  
이 경우, 클라이언트에 필요한 데이터만을 포함한 새로운 객체를 생성하여 반환한다.  
이 방법은 응답 데이터를 더 잘 제어할 수 있게 해주며, 비즈니스 로직을 API 레이어로부터 분리하는 데에도 도움이 된다.

```java
@Getter
@AllArgsConstructor
public class BoardDto {
  // ...
  
  private List<String> imageList;

  public static BoardDto fromEntity(Board entity) {
    // 이미지가 존재할 시, 해당 연관된 데이터를 모두 다 불러오기에 imageUrl만 따로 분리
    List<String> imageUrl = new ArrayList<>();
    if (!entity.getImageList().isEmpty()) {
      for (Image image : entity.getImageList()) {
        imageUrl.add(image.getImgUrl());
      }
    }

    return new BoardDto(
      // ...
      imageUrl
    );
  }
}
```

</details>