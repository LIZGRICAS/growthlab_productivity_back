package com.example.clevertap.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CleverTapClient {
    @Value("${clevertap.accountId}")
    private String accountId;
    @Value("${clevertap.passcode}")
    private String passcode;
    @Value("${clevertap.endpoint}")
    private String endpoint;

    private final RestTemplate rest = new RestTemplate();

    public void uploadProfile(Map<String, Object> profilePayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-CleverTap-Account-Id", accountId);
        headers.set("X-CleverTap-Passcode", passcode);

        HttpEntity<Object> req = new HttpEntity<>(profilePayload, headers);
        rest.postForEntity(endpoint, req, String.class);
    }

    public void uploadEvent(Map<String, Object> eventPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-CleverTap-Account-Id", accountId);
        headers.set("X-CleverTap-Passcode", passcode);

        HttpEntity<Object> req = new HttpEntity<>(eventPayload, headers);
        rest.postForEntity(endpoint, req, String.class);
    }
}
