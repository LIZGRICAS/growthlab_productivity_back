package com.example.clevertap.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = "security.jwt.secret=01234567890123456789012345678901")
class JwtServiceUnitTest {
    @Autowired
    private JwtService jwtService;

    @Test
    void generateAndValidateToken() {
        String token = jwtService.generateToken("user@example.com");
        Assertions.assertNotNull(token);
        Assertions.assertTrue(jwtService.validateToken(token));
        Assertions.assertEquals("user@example.com", jwtService.getUsername(token));
    }
}
