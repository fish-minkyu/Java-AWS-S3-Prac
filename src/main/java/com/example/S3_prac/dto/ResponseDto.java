package com.example.S3_prac.dto;

import com.example.S3_prac.entity.Board;
import com.example.S3_prac.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResponseDto {
  private Long id;
  private String title;
  private String content;

  private List<Image> imageList;

  public static ResponseDto fromEntity(Board entity) {

    return new ResponseDto(
      entity.getId(),
      entity.getTitle(),
      entity.getContent(),
      entity.getImageList()
    );
  }
}
