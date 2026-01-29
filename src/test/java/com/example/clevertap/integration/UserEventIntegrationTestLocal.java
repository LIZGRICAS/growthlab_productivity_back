package com.example.clevertap.integration;

import com.example.clevertap.dto.EventRequest;
import com.example.clevertap.dto.UserRequest;
import com.example.clevertap.service.TestCleverTapClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserEventIntegrationTestLocal {

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.example.clevertap.client.CleverTapClient cleverTapClient() {
            return new TestCleverTapClient();
        }
    }

    @Test
    void registerCreateEventAndList_localStubbedCleverTap() {
        // Register a user
        UserRequest ur = new UserRequest();
        ur.setIdentity("intg-user-1");
        ur.setEmail("intg1@example.com");
        ur.setName("Integration User");
        ur.setPassword("pass123");

        ResponseEntity<String> regResp = restTemplate.postForEntity("/api/auth/register", ur, String.class);
        Assertions.assertEquals(HttpStatus.OK, regResp.getStatusCode());

        // Extract token from response body (AuthResponse JSON: {"token":"..."})
        String body = regResp.getBody();
        Assertions.assertNotNull(body);
        String token = null;
        if (body.contains("token")) {
            int i = body.indexOf(':');
            token = body.substring(i + 1).replaceAll("[\"{} ]", "");
        }
        Assertions.assertNotNull(token);

        // Create an event with Authorization header
        EventRequest er = new EventRequest();
        // for register flow user id may be returned in DB; find events API expects userId - use 1
        er.setUserId(1L);
        er.setEventName("integration-event");
        er.setPropertiesJson("{\"source\":\"integration-test\"}");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EventRequest> eventReq = new HttpEntity<>(er, headers);

        ResponseEntity<String> evResp = restTemplate.exchange("/api/events", HttpMethod.POST, eventReq, String.class);
        Assertions.assertEquals(HttpStatus.OK, evResp.getStatusCode());

        // List events for user
        HttpEntity<Void> getReq = new HttpEntity<>(headers);
        ResponseEntity<String> listResp = restTemplate.exchange("/api/users/1/events", HttpMethod.GET, getReq, String.class);
        Assertions.assertEquals(HttpStatus.OK, listResp.getStatusCode());
        Assertions.assertTrue(listResp.getBody() != null && listResp.getBody().contains("integration-event"));
    }
}
