package com.example.clevertap.service;

import com.example.clevertap.client.CleverTapClient;
import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.exception.ResourceNotFoundException;
import com.example.clevertap.mapper.UserMapper;
import com.example.clevertap.model.User;
import com.example.clevertap.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CleverTapClient clevertap;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CleverTapClient clevertap) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clevertap = clevertap;
    }

    public User createUser(UserRequest req) {
        userRepository.findByIdentity(req.getIdentity()).ifPresent(u -> { throw new IllegalArgumentException("identity already exists"); });
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> { throw new IllegalArgumentException("email already exists"); });
        User u = UserMapper.toEntity(req);
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        User saved = userRepository.save(u);

        // send to CleverTap
        Map<String, Object> profile = new HashMap<>();
        Map<String, Object> profileProps = new HashMap<>();
        profileProps.put("Name", saved.getName());
        profileProps.put("Email", saved.getEmail());
        profileProps.put("Identity", saved.getIdentity());
        profile.put("d", new Object[]{profileProps});
        try { clevertap.uploadProfile(profile); } catch (Exception ignored) {}

        return saved;
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
