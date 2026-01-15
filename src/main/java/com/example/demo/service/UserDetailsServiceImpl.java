package com.example.demo.service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.model.UserDetailImpl;
import com.example.demo.repository.UserRepository;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository urepo;

    public UserDetailsServiceImpl(UserRepository urepo) {
        this.urepo = urepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = urepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new UserDetailImpl(user);
    }
}
