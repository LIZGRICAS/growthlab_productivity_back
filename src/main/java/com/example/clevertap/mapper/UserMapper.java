package com.example.clevertap.mapper;

import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.model.User;

public class UserMapper {
    public static User toEntity(UserRequest req) {
        User u = new User();
        u.setIdentity(req.getIdentity());
        u.setEmail(req.getEmail());
        u.setName(req.getName());
        u.setPassword(req.getPassword());
        return u;
    }
}
