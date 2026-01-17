package com.example.demo.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentController Tests")
class AssignmentControllerTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SolutionRepository solutionRepository;

    @InjectMocks
    private AssignmentController assignmentController;

    private User adminUser;
    private User studentUser;
    private Assignment testAssignment;
    private Solution testSolution;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId((int) 1L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        studentUser = new User();
        studentUser.setId((int) 2L);
        studentUser.setUsername("student");
        studentUser.setEmail("student@example.com");
        studentUser.setRole(Role.STUDENT);

        testAssignment = new Assignment();
        testAssignment.setId(1L);
        testAssignment.setTitle("Java Basics");
        testAssignment.setQuestions(Arrays.asList("What is JVM?", "What is polymorphism?"));

        testSolution = new Solution();
        testSolution.setId(1L);
        testSolution.setAssignment(testAssignment);
        testSolution.setStudent(studentUser);
        testSolution.setAnswers(Arrays.asList("Answer 1", "Answer 2"));
    }

    // ========== CREATE ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Should create assignment successfully by admin")
    void testCreateAssignment_Success() {
        // Arrange
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Java Basics");
        request.setQuestions(Arrays.asList("What is JVM?", "What is polymorphism?"));

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // Act
        ResponseEntity<String> response = assignmentController.createAssignment(request, adminUserDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Assignment created successfully", response.getBody());

        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should fail to create assignment when not authenticated")
    void testCreateAssignment_NotAuthenticated() {
        // Arrange
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Java Basics");

        // Act
        ResponseEntity<String> response = assignmentController.createAssignment(request, null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should fail to create assignment when user is not admin")
    void testCreateAssignment_NotAdmin() {
        // Arrange
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Java Basics");

        UserDetailImpl studentUserDetails = new UserDetailImpl(studentUser);

        // Act
        ResponseEntity<String> response = assignmentController.createAssignment(request, studentUserDetails);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only admin can create assignments", response.getBody());

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    // ========== GET ALL ASSIGNMENTS TESTS ==========

    @Test
    @DisplayName("Should get all assignments successfully")
    void testGetAssignments_Success() {
        // Arrange
        List<Assignment> assignments = Arrays.asList(testAssignment);
        UserDetailImpl studentUserDetails = new UserDetailImpl(studentUser);

        when(assignmentRepository.findAll()).thenReturn(assignments);

        // Act
        ResponseEntity<List<Assignment>> response = assignmentController.getAssignments(studentUserDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Java Basics", response.getBody().get(0).getTitle());

        verify(assignmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should fail to get assignments when not authenticated")
    void testGetAssignments_NotAuthenticated() {
        // Act
        ResponseEntity<List<Assignment>> response = assignmentController.getAssignments(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(assignmentRepository, never()).findAll();
    }

    // ========== SUBMIT SOLUTION TESTS ==========

    @Test
    @DisplayName("Should submit solution successfully by student")
    void testSubmitSolution_Success() {
        // Arrange
        SolutionRequest request = new SolutionRequest();
        request.setAssignmentId(1L);
        request.setAnswers(Arrays.asList("Answer 1", "Answer 2"));

        UserDetailImpl studentUserDetails = new UserDetailImpl(studentUser);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(solutionRepository.save(any(Solution.class))).thenReturn(testSolution);

        // Act
        ResponseEntity<String> response = assignmentController.submitSolution(request, studentUserDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Solution submitted successfully", response.getBody());

        verify(assignmentRepository, times(1)).findById(1L);
        verify(solutionRepository, times(1)).save(any(Solution.class));
    }

    @Test
    @DisplayName("Should fail to submit solution when not authenticated")
    void testSubmitSolution_NotAuthenticated() {
        // Arrange
        SolutionRequest request = new SolutionRequest();
        request.setAssignmentId(1L);

        // Act
        ResponseEntity<String> response = assignmentController.submitSolution(request, null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());

        verify(assignmentRepository, never()).findById(anyLong());
        verify(solutionRepository, never()).save(any(Solution.class));
    }

    @Test
    @DisplayName("Should fail to submit solution when user is not student")
    void testSubmitSolution_NotStudent() {
        // Arrange
        SolutionRequest request = new SolutionRequest();
        request.setAssignmentId(1L);

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        // Act
        ResponseEntity<String> response = assignmentController.submitSolution(request, adminUserDetails);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only students can submit solutions", response.getBody());

        verify(assignmentRepository, never()).findById(anyLong());
        verify(solutionRepository, never()).save(any(Solution.class));
    }

    @Test
    @DisplayName("Should fail to submit solution when assignment not found")
    void testSubmitSolution_AssignmentNotFound() {
        // Arrange
        SolutionRequest request = new SolutionRequest();
        request.setAssignmentId(999L);

        UserDetailImpl studentUserDetails = new UserDetailImpl(studentUser);

        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assignmentController.submitSolution(request, studentUserDetails);
        });

        verify(assignmentRepository, times(1)).findById(999L);
        verify(solutionRepository, never()).save(any(Solution.class));
    }

    // ========== GIVE MARKS TESTS ==========

    @Test
    @DisplayName("Should give marks successfully by admin")
    void testGiveMarks_Success() {
        // Arrange
        MarksRequest request = new MarksRequest();
        request.setSolutionId(1L);
        request.setMarks(85);

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(solutionRepository.findById(1L)).thenReturn(Optional.of(testSolution));
        when(solutionRepository.save(any(Solution.class))).thenReturn(testSolution);

        // Act
        ResponseEntity<String> response = assignmentController.giveMarks(request, adminUserDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Marks updated successfully", response.getBody());

        verify(solutionRepository, times(1)).findById(1L);
        verify(solutionRepository, times(1)).save(any(Solution.class));
    }

    @Test
    @DisplayName("Should fail to give marks when not authenticated")
    void testGiveMarks_NotAuthenticated() {
        // Arrange
        MarksRequest request = new MarksRequest();
        request.setSolutionId(1L);
        request.setMarks(85);

        // Act
        ResponseEntity<String> response = assignmentController.giveMarks(request, null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());

        verify(solutionRepository, never()).findById(anyLong());
        verify(solutionRepository, never()).save(any(Solution.class));
    }

    @Test
    @DisplayName("Should fail to give marks when user is not admin")
    void testGiveMarks_NotAdmin() {
        // Arrange
        MarksRequest request = new MarksRequest();
        request.setSolutionId(1L);
        request.setMarks(85);

        UserDetailImpl studentUserDetails = new UserDetailImpl(studentUser);

        // Act
        ResponseEntity<String> response = assignmentController.giveMarks(request, studentUserDetails);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only admin can give marks", response.getBody());

        verify(solutionRepository, never()).findById(anyLong());
        verify(solutionRepository, never()).save(any(Solution.class));
    }

    @Test
    @DisplayName("Should fail to give marks when solution not found")
    void testGiveMarks_SolutionNotFound() {
        // Arrange
        MarksRequest request = new MarksRequest();
        request.setSolutionId(999L);
        request.setMarks(85);

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(solutionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assignmentController.giveMarks(request, adminUserDetails);
        });

        verify(solutionRepository, times(1)).findById(999L);
        verify(solutionRepository, never()).save(any(Solution.class));
    }
}