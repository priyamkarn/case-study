package com.example.demo.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserDetailImpl;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // LOGIN (open to everyone)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        logger.info("Login attempt for username: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    // REGISTER (only admin)
  @PostMapping("/register")
public ResponseEntity<String> register(
        @RequestBody RegisterRequest request,
        @AuthenticationPrincipal UserDetailImpl currentUserDetails) {
    
    // Check if user is authenticated
    logger.info("Registration attempt for username: {}", request.getUsername());

    if (currentUserDetails == null) {
        return ResponseEntity.status(401).body("Authentication required");
    }
    
    // Get the actual User entity
    User currentUser = currentUserDetails.getUser();
    logger.debug("Registration request by admin: {}", currentUser.getUsername());

    // Check if user is admin
    if (!currentUser.getRole().equals(Role.ADMIN)) {
        logger.warn("Non-admin user {} attempted to register new user", currentUser.getUsername());
        return ResponseEntity.status(403).body("Only admins can register new users");
    }
    
    // Check for existing username/email
    if (userRepository.existsByUsername(request.getUsername())) {
        logger.warn("Registration failed: Username {} already exists", request.getUsername());
        return ResponseEntity.badRequest().body("Username already exists");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
        logger.warn("Registration failed: Email {} already exists", request.getEmail());
        return ResponseEntity.badRequest().body("Email already exists");
    }

    // Create and save user
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole() != null ? request.getRole() : Role.STUDENT); 

    userRepository.save(user);
    logger.info("User {} registered successfully by admin {} with role {}", 
                    user.getUsername(), currentUser.getUsername(), user.getRole());
    return ResponseEntity.ok("User registered successfully by admin");
}
}
