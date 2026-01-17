package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.model.UserDetailImpl;
import com.example.demo.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository urepo;

    public UserDetailsServiceImpl(UserRepository urepo) {
        this.urepo = urepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: '{}'", username);

        try {
            User user = urepo.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found: '{}'", username);
                        return new UsernameNotFoundException("User not found: " + username);
                    });

            logger.info("User '{}' loaded successfully with role: {}", username, user.getRole());
            logger.trace("User details - ID: {}, Email: {}, Role: {}", 
                         user.getId(), user.getEmail(), user.getRole());

            UserDetailImpl userDetails = new UserDetailImpl(user);
            logger.debug("UserDetails object created for user '{}' with authorities: {}", 
                         username, userDetails.getAuthorities());

            return userDetails;
        } catch (UsernameNotFoundException e) {
            logger.warn("Failed to load user: '{}'", username);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while loading user '{}': {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
}