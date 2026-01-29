package com.example.clevertap.service;

import com.example.clevertap.dto.AuthResponse;
import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.clevertap.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserService userService, AuthenticationManager authManager, JwtService jwtService) {
        this.userService = userService;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(UserRequest req) {
        User u = userService.createUser(req);
        String token = jwtService.generateToken(u.getEmail());
        return new AuthResponse(token, u.getId());
    }

    public AuthResponse login(String email, String password) {
        try {
            Authentication a = authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            String token = jwtService.generateToken(email);
            return new AuthResponse(token);
        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
}
