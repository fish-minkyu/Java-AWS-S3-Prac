package com.example.S3_prac.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Board {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String content;

  @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
  @JsonManagedReference
  @Setter
  private List<Image> imageList = new ArrayList<>();

  public void addImage(Image image) {
    // 현재 Board 인스턴스에 Image 객체를 추가
    this.imageList.add(image);
    // Image 객체의 Board 참조를 현재 Board 인스턴스로 설정
    image.setBoard(this);
  }

  // imageList NullPointException 발생으로 인한 CustomBuilder
  public static BoardBuilder customBuilder() {
    return builder()
      .imageList(new ArrayList<>());
  }
}
