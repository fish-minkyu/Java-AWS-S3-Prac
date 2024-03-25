package com.example.S3_prac.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marvin.image.MarvinImage;
import org.marvinproject.image.transform.scale.Scale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3FileService {
  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Transactional
  public List<String> uploadIntoS3(String folder, List<MultipartFile> multipartFileList) {
    log.info("uploadIntoS3 tx start");
    List<String> imgUrlList = new ArrayList<>();

    multipartFileList.forEach(file -> {
      if (Objects.requireNonNull(file.getContentType()).contains("image")) {

        String filename = createFilename(file.getOriginalFilename());
        String fileFormat = file.getContentType().substring(file.getContentType().lastIndexOf("/")+1);
        log.info("fileFormat: : "+fileFormat);
//                String fileExtension = this.getFileExtension(filename);
        if (folder.equals("/profileImg")) {
          file = resizeImage(filename, fileFormat, file, 500);
        } else if (folder.equals("/artistImg")) {
          file = resizeImage(filename, fileFormat, file, 750);
        }

        // 리사이징 후 S3에 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        log.info("contentType: " + file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
          amazonS3.putObject(
            new PutObjectRequest(bucket + folder,
              filename,
              inputStream,
              metadata)
              .withCannedAcl(CannedAccessControlList.PublicRead)
          );
          // 성공적 업로드시 imgUrlList에 추가
          imgUrlList.add(amazonS3.getUrl(bucket + folder, filename).toString());
        } catch (IOException e) {
          log.warn(e.getMessage() + " : 이미지 업로드에 실패하였습니다. ");
        }
      }
    });
    log.info("uploadIntoS3 tx end");
    return imgUrlList;
  }

  private String createFilename(String filename) {
//        return UUID.randomUUID().toString();
    return UUID.randomUUID().toString().concat(getFileExtension(filename));
  }

  private String getFileExtension(String filename) {
    if (filename.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일입니다.");
    }
    return filename.substring(filename.lastIndexOf("."));
  }

  @Transactional
  public void deleteFile(String folder, String filename) {
    amazonS3.deleteObject(bucket+folder, filename);
  }

  @Transactional
  public MultipartFile resizeImage(String filename, String fileFormat, MultipartFile originalFile, int targetWidth) {
    try {
      // MultipartFile -> BufferedImage 형태로 변환
      BufferedImage image = ImageIO.read(originalFile.getInputStream());
      int originWidth = image.getWidth();
      int originHeight = image.getHeight();

      if (originWidth < targetWidth) {
        return originalFile;
      }

      MarvinImage marvinImage = new MarvinImage(image);

      Scale scale = new Scale();
      scale.load();
      scale.setAttribute("newWidth", targetWidth);
      scale.setAttribute("newHeight", targetWidth * originHeight / originWidth);
      scale.process(marvinImage.clone(), marvinImage, null, null, false);

      BufferedImage imageNoAlpha = marvinImage.getBufferedImageNoAlpha();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(imageNoAlpha, fileFormat, baos);
      baos.flush();

      return new CustomMultipartFile(filename, fileFormat, originalFile.getContentType(), baos.toByteArray());

    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 리사이징에 실패했습니다.");
    }
  }
}
