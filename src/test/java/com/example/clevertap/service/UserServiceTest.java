package com.example.clevertap.service;

import com.example.clevertap.client.CleverTapClient;
import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.model.User;
import com.example.clevertap.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class UserServiceTest {
    @Test
    public void createUser_success() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        org.springframework.security.crypto.password.PasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        com.example.clevertap.client.CleverTapClient client = new TestCleverTapClient();

        UserService svc = new UserService(repo, enc, client);
        UserRequest req = new UserRequest();
        req.setIdentity("id-123");
        req.setEmail("a@b.com");
        req.setName("Name");
        req.setPassword("pass");

        Mockito.when(repo.findByIdentity("id-123")).thenReturn(Optional.empty());
        Mockito.when(repo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        // encoder is real BCrypt; no need to stub encode
        Mockito.when(repo.save(Mockito.any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = svc.createUser(req);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("id-123", saved.getIdentity());
    }
}
