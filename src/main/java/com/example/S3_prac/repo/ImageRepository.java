package com.example.S3_prac.repo;

import com.example.S3_prac.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

  List<Image> findAllByBoard_Id(Long boardId);
}
