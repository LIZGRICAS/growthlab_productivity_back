package com.example.clevertap.controller;

import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.model.User;
import com.example.clevertap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest req) {
        User u = userService.createUser(req);
        return ResponseEntity.ok(u);
    }
}
