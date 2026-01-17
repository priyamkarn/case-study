package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserDetailImpl;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void shouldGenerateAndValidateToken() {
        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.STUDENT);

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.STUDENT);

        String token = jwtUtil.generateToken(user);
        UserDetails userDetails = new UserDetailImpl(user);

        assertTrue(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void shouldRejectTokenForWrongUser() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setRole(Role.STUDENT);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setRole(Role.STUDENT);

        String token = jwtUtil.generateToken(user1);
        UserDetails wrongUserDetails = new UserDetailImpl(user2);

        assertFalse(jwtUtil.isTokenValid(token, wrongUserDetails));
    }
}