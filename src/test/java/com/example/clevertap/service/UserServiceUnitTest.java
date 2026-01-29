package com.example.clevertap.service;

import com.example.clevertap.client.CleverTapClient;
import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.model.User;
import com.example.clevertap.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

class UserServiceUnitTest {
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private CleverTapClient client;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        client = new TestCleverTapClient();
        userService = new UserService(userRepository, passwordEncoder, client);
    }

    @Test
    void createUser_conflictOnIdentity() {
        UserRequest req = new UserRequest(); req.setIdentity("id1"); req.setEmail("a@b.com"); req.setName("n"); req.setPassword("p");
        Mockito.when(userRepository.findByIdentity("id1")).thenReturn(Optional.of(new User()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.createUser(req));
    }

    @Test
    void createUser_success_callsCleverTap() {
        UserRequest req = new UserRequest(); req.setIdentity("id2"); req.setEmail("c@d.com"); req.setName("n"); req.setPassword("p");
        Mockito.when(userRepository.findByIdentity("id2")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail("c@d.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(5L);
            return u;
        });

        User saved = userService.createUser(req);
        Assertions.assertNotNull(saved.getId());
        TestCleverTapClient stub = (TestCleverTapClient) client;
        Assertions.assertEquals(1, stub.getProfileCalls());
        Assertions.assertNotNull(stub.getLastProfilePayload());
    }
}
