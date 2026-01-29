package com.example.clevertap.integration;

import com.example.clevertap.dto.EventRequest;
import com.example.clevertap.dto.UserRequest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QAIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void runAgainstCleverTapIfCredentialsPresent() {
        String acct = System.getenv("CLEVERTAP_ACCOUNT_ID");
        String token = System.getenv("CLEVERTAP_TOKEN");
        // skip if placeholders
        Assumptions.assumeTrue(acct != null && token != null && !acct.startsWith("TEST-"), "CleverTap credentials not provided or are placeholders");

        // create user via /api/auth/register (returns token and userId)
        UserRequest ur = new UserRequest();
        ur.setIdentity("qa-run-" + System.currentTimeMillis());
        ur.setEmail("qa-run+" + System.currentTimeMillis() + "@example.com");
        ur.setName("QA Run");
        ur.setPassword("Pass123!");

        ResponseEntity<AuthResponseWrapper> reg = restTemplate.postForEntity("/api/auth/register", ur, AuthResponseWrapper.class);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, reg.getStatusCode());
        String jwt = reg.getBody().token;
        Long userId = reg.getBody().userId;
        org.junit.jupiter.api.Assertions.assertNotNull(jwt);
        org.junit.jupiter.api.Assertions.assertNotNull(userId);

        // post event
        EventRequest er = new EventRequest();
        er.setUserId(userId);
        er.setEventName("qa-integration-event");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EventRequest> req = new HttpEntity<>(er, headers);

        ResponseEntity<String> evRes = restTemplate.exchange("/api/events", HttpMethod.POST, req, String.class);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, evRes.getStatusCode());

        // list events
        HttpEntity<Void> getReq = new HttpEntity<>(headers);
        ResponseEntity<String> list = restTemplate.exchange("/api/users/" + userId + "/events", HttpMethod.GET, getReq, String.class);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, list.getStatusCode());
    }

    // Helper wrapper to map the AuthResponse JSON with token and userId
    static class AuthResponseWrapper { public String token; public Long userId; }
}
