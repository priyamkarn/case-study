package com.example.demo.controller;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserDetailImpl;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private User adminUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId((int) 1L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole(Role.ADMIN);

        studentUser = new User();
        studentUser.setId((int) 2L);
        studentUser.setUsername("student");
        studentUser.setEmail("student@example.com");
        studentUser.setPassword("encodedPassword");
        studentUser.setRole(Role.STUDENT);
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken(adminUser)).thenReturn("mock-jwt-token");

        // Act
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getToken());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("admin");
        verify(jwtUtil, times(1)).generateToken(adminUser);
    }

    @Test
    @DisplayName("Should fail login with invalid credentials")
    void testLogin_InvalidCredentials() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authController.login(request);
        });

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found after authentication")
    void testLogin_UserNotFoundAfterAuth() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    // ========== REGISTER TESTS ==========

    @Test
    @DisplayName("Should register user successfully by admin")
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newstudent");
        request.setEmail("newstudent@example.com");
        request.setPassword("password123");
        request.setRole(Role.STUDENT);

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("newstudent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<String> response = authController.register(request, adminUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully by admin", response.getBody());

        verify(userRepository, times(1)).existsByUsername("newstudent");
        verify(userRepository, times(1)).existsByEmail("newstudent@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when user is not authenticated")
    void testRegister_NotAuthenticated() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newstudent");
        request.setEmail("newstudent@example.com");
        request.setPassword("password123");

        // Act
        ResponseEntity<String> response = authController.register(request, null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when user is not admin")
    void testRegister_NotAdmin() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newstudent");
        request.setEmail("newstudent@example.com");
        request.setPassword("password123");

        UserDetailImpl studentUserDetails = new UserDetailImpl(studentUser);

        // Act
        ResponseEntity<String> response = authController.register(request, studentUserDetails);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only admins can register new users", response.getBody());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when username already exists")
    void testRegister_UsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act
        ResponseEntity<String> response = authController.register(request, adminUserDetails);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());

        verify(userRepository, times(1)).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void testRegister_EmailExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act
        ResponseEntity<String> response = authController.register(request, adminUserDetails);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already exists", response.getBody());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should register with default STUDENT role when role is null")
    void testRegister_DefaultRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newstudent");
        request.setEmail("newstudent@example.com");
        request.setPassword("password123");
        request.setRole(null); // No role specified

        UserDetailImpl adminUserDetails = new UserDetailImpl(adminUser);

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("newstudent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(Role.STUDENT, savedUser.getRole());
            return savedUser;
        });

        // Act
        ResponseEntity<String> response = authController.register(request, adminUserDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(any(User.class));
    }
}