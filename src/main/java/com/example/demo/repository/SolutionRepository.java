package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Solution;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
    List<Solution> findByStudentId(Long studentId);
}
