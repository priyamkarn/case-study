package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AssignmentRequest;
import com.example.demo.dto.MarksRequest;
import com.example.demo.dto.SolutionRequest;
import com.example.demo.model.Assignment;
import com.example.demo.model.Role;
import com.example.demo.model.Solution;
import com.example.demo.model.User;
import com.example.demo.model.UserDetailImpl;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.repository.SolutionRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final SolutionRepository solutionRepository;

    // Admin posts assignment
    @PostMapping("/create")
    public ResponseEntity<String> createAssignment(
            @RequestBody AssignmentRequest request,
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        if (currentUserDetails == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        User currentUser = currentUserDetails.getUser();
        
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).body("Only admin can create assignments");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setQuestions(request.getQuestions());

        assignmentRepository.save(assignment);
        return ResponseEntity.ok("Assignment created successfully");
    }

    // Student gets all assignments
    @GetMapping("/all")
    public ResponseEntity<List<Assignment>> getAssignments(
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        if (currentUserDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.ok(assignmentRepository.findAll());
    }

    // Student submits solution
    @PostMapping("/submit")
    public ResponseEntity<String> submitSolution(
            @RequestBody SolutionRequest request,
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        if (currentUserDetails == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        User currentUser = currentUserDetails.getUser();
        
        if (!currentUser.getRole().equals(Role.STUDENT)) {
            return ResponseEntity.status(403).body("Only students can submit solutions");
        }

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Solution solution = new Solution();
        solution.setAssignment(assignment);
        solution.setStudent(currentUser);
        solution.setAnswers(request.getAnswers());

        solutionRepository.save(solution);
        return ResponseEntity.ok("Solution submitted successfully");
    }

    // Admin gives marks
    @PostMapping("/mark")
    public ResponseEntity<String> giveMarks(
            @RequestBody MarksRequest request,
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        if (currentUserDetails == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        User currentUser = currentUserDetails.getUser();
        
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).body("Only admin can give marks");
        }

        Solution solution = solutionRepository.findById(request.getSolutionId())
                .orElseThrow(() -> new RuntimeException("Solution not found"));

        solution.setMarks(request.getMarks());
        solutionRepository.save(solution);
        return ResponseEntity.ok("Marks updated successfully");
    }
}