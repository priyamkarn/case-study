package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class AssignmentRequest {
    private String title;
    private List<String> questions;
}
