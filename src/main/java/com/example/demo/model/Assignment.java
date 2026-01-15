package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Assignment title

    @ElementCollection
    private List<String> questions; // List of questions

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Solution> solutions; // Student solutions
}
