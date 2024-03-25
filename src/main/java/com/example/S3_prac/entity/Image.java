package com.example.S3_prac.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Image {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String url;

  @ManyToOne(fetch = FetchType.LAZY) // 기본 타입 Eager
  @JoinColumn(name = "board_id")
  @Setter
  private Board board;
}
