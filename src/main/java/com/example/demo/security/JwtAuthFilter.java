package com.example.demo.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        
        logger.debug("Processing request: {} {}", method, requestURI);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found for request: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            logger.trace("JWT token extracted from Authorization header");
            
            String username = jwtUtil.extractUsername(token);
            logger.debug("Extracted username '{}' from JWT token", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Attempting to authenticate user '{}'", username);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.trace("User details loaded for username '{}'", username);
                
                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("User '{}' authenticated successfully for request: {} {} with authorities: {}", 
                                username, method, requestURI, userDetails.getAuthorities());
                } else {
                    logger.warn("Invalid JWT token for user '{}' on request: {} {}", username, method, requestURI);
                }
            } else if (username == null) {
                logger.warn("Could not extract username from JWT token for request: {} {}", method, requestURI);
            } else {
                logger.trace("User '{}' already authenticated in security context", username);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("Expired JWT token for request: {} {} - {}", method, requestURI, e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.warn("Malformed JWT token for request: {} {} - {}", method, requestURI, e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token for request: {} {} - {}", method, requestURI, e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.warn("Invalid JWT signature for request: {} {} - {}", method, requestURI, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid JWT token (illegal argument) for request: {} {} - {}", method, requestURI, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during JWT authentication for request: {} {} - {}", 
                         method, requestURI, e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
}