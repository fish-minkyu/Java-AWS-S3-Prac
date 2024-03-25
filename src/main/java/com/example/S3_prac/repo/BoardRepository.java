package com.example.S3_prac.repo;

import com.example.S3_prac.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
