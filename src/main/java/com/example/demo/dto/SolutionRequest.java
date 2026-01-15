package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class SolutionRequest {
    private Long assignmentId;
    private List<String> answers;
}
