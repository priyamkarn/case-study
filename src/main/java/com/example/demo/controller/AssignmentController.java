package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final SolutionRepository solutionRepository;

    // 1️⃣ Admin posts assignment
    @PostMapping("/create")
    public ResponseEntity<String> createAssignment(@RequestBody AssignmentRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).body("Only admin can create assignments");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setQuestions(request.getQuestions());

        assignmentRepository.save(assignment);
        return ResponseEntity.ok("Assignment created successfully");
    }

    // 2️⃣ Student gets all assignments
    @GetMapping("/all")
    public List<Assignment> getAssignments(@AuthenticationPrincipal User currentUser) {
        // optionally filter for students only
        return assignmentRepository.findAll();
    }

    // 3️⃣ Student submits solution
    @PostMapping("/submit")
    public ResponseEntity<String> submitSolution(@RequestBody SolutionRequest request,
                                                 @AuthenticationPrincipal User currentUser) {
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

    // 4️⃣ Admin gives marks
    @PostMapping("/mark")
    public ResponseEntity<String> giveMarks(@RequestBody MarksRequest request,
                                            @AuthenticationPrincipal User currentUser) {
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
