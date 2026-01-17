package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    private final AssignmentRepository assignmentRepository;
    private final SolutionRepository solutionRepository;

    // Admin posts assignment
    @PostMapping("/create")
    public ResponseEntity<String> createAssignment(
            @RequestBody AssignmentRequest request,
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        logger.info("Assignment creation request received for title: '{}'", request.getTitle());
        
        if (currentUserDetails == null) {
            logger.warn("Unauthenticated attempt to create assignment: '{}'", request.getTitle());
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        User currentUser = currentUserDetails.getUser();
        logger.debug("User '{}' with role '{}' attempting to create assignment", 
                     currentUser.getUsername(), currentUser.getRole());
        
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            logger.warn("Non-admin user '{}' attempted to create assignment", currentUser.getUsername());
            return ResponseEntity.status(403).body("Only admin can create assignments");
        }

        try {
            Assignment assignment = new Assignment();
            assignment.setTitle(request.getTitle());
            assignment.setQuestions(request.getQuestions());

            Assignment savedAssignment = assignmentRepository.save(assignment);
            logger.info("Assignment '{}' (ID: {}) created successfully by admin '{}'", 
                        savedAssignment.getTitle(), savedAssignment.getId(), currentUser.getUsername());
            
            return ResponseEntity.ok("Assignment created successfully");
        } catch (Exception e) {
            logger.error("Error creating assignment '{}' by user '{}'", 
                         request.getTitle(), currentUser.getUsername(), e);
            throw e;
        }
    }

    // Student gets all assignments
    @GetMapping("/all")
    public ResponseEntity<List<Assignment>> getAssignments(
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        logger.info("Request to fetch all assignments");
        
        if (currentUserDetails == null) {
            logger.warn("Unauthenticated attempt to view assignments");
            return ResponseEntity.status(401).build();
        }
        
        User currentUser = currentUserDetails.getUser();
        logger.debug("User '{}' with role '{}' fetching assignments", 
                     currentUser.getUsername(), currentUser.getRole());
        
        try {
            List<Assignment> assignments = assignmentRepository.findAll();
            logger.info("Retrieved {} assignments for user '{}'", 
                        assignments.size(), currentUser.getUsername());
            
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error fetching assignments for user '{}'", currentUser.getUsername(), e);
            throw e;
        }
    }

    // Student submits solution
    @PostMapping("/submit")
    public ResponseEntity<String> submitSolution(
            @RequestBody SolutionRequest request,
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        logger.info("Solution submission request for assignment ID: {}", request.getAssignmentId());
        
        if (currentUserDetails == null) {
            logger.warn("Unauthenticated attempt to submit solution for assignment ID: {}", 
                        request.getAssignmentId());
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        User currentUser = currentUserDetails.getUser();
        logger.debug("User '{}' with role '{}' attempting to submit solution for assignment ID: {}", 
                     currentUser.getUsername(), currentUser.getRole(), request.getAssignmentId());
        
        if (!currentUser.getRole().equals(Role.STUDENT)) {
            logger.warn("Non-student user '{}' (role: {}) attempted to submit solution", 
                        currentUser.getUsername(), currentUser.getRole());
            return ResponseEntity.status(403).body("Only students can submit solutions");
        }

        try {
            Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> {
                        logger.error("Assignment not found with ID: {} requested by student '{}'", 
                                     request.getAssignmentId(), currentUser.getUsername());
                        return new RuntimeException("Assignment not found");
                    });

            logger.debug("Student '{}' submitting solution for assignment '{}' (ID: {})", 
                         currentUser.getUsername(), assignment.getTitle(), assignment.getId());

            Solution solution = new Solution();
            solution.setAssignment(assignment);
            solution.setStudent(currentUser);
            solution.setAnswers(request.getAnswers());

            Solution savedSolution = solutionRepository.save(solution);
            logger.info("Solution (ID: {}) submitted successfully by student '{}' for assignment '{}' (ID: {})", 
                        savedSolution.getId(), currentUser.getUsername(), 
                        assignment.getTitle(), assignment.getId());
            
            return ResponseEntity.ok("Solution submitted successfully");
        } catch (RuntimeException e) {
            logger.error("Error submitting solution for assignment ID: {} by student '{}'", 
                         request.getAssignmentId(), currentUser.getUsername(), e);
            throw e;
        }
    }

    // Admin gives marks
    @PostMapping("/mark")
    public ResponseEntity<String> giveMarks(
            @RequestBody MarksRequest request,
            @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
        
        logger.info("Marks assignment request for solution ID: {} with marks: {}", 
                    request.getSolutionId(), request.getMarks());
        
        if (currentUserDetails == null) {
            logger.warn("Unauthenticated attempt to assign marks for solution ID: {}", 
                        request.getSolutionId());
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        User currentUser = currentUserDetails.getUser();
        logger.debug("User '{}' with role '{}' attempting to assign marks for solution ID: {}", 
                     currentUser.getUsername(), currentUser.getRole(), request.getSolutionId());
        
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            logger.warn("Non-admin user '{}' (role: {}) attempted to assign marks", 
                        currentUser.getUsername(), currentUser.getRole());
            return ResponseEntity.status(403).body("Only admin can give marks");
        }

        try {
            Solution solution = solutionRepository.findById(request.getSolutionId())
                    .orElseThrow(() -> {
                        logger.error("Solution not found with ID: {} requested by admin '{}'", 
                                     request.getSolutionId(), currentUser.getUsername());
                        return new RuntimeException("Solution not found");
                    });

            Integer previousMarks = solution.getMarks();
            solution.setMarks(request.getMarks());
            solutionRepository.save(solution);
            
            logger.info("Admin '{}' assigned {} marks to solution ID: {} for student '{}' (assignment: '{}', previous marks: {})", 
                        currentUser.getUsername(), request.getMarks(), solution.getId(), 
                        solution.getStudent().getUsername(), solution.getAssignment().getTitle(), 
                        previousMarks);
            
            return ResponseEntity.ok("Marks updated successfully");
        } catch (RuntimeException e) {
            logger.error("Error assigning marks for solution ID: {} by admin '{}'", 
                         request.getSolutionId(), currentUser.getUsername(), e);
            throw e;
        }
    }
}