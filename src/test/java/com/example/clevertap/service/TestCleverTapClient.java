package com.example.clevertap.service;

public class TestCleverTapClient extends com.example.clevertap.client.CleverTapClient {
    private int profileCalls = 0;
    private int eventCalls = 0;
    private java.util.Map<String, Object> lastProfilePayload;
    private java.util.Map<String, Object> lastEventPayload;

    @Override
    public void uploadProfile(java.util.Map<String, Object> profilePayload) {
        this.profileCalls++;
        this.lastProfilePayload = profilePayload;
    }

    @Override
    public void uploadEvent(java.util.Map<String, Object> eventPayload) {
        this.eventCalls++;
        this.lastEventPayload = eventPayload;
    }

    public int getProfileCalls() {
        return profileCalls;
    }

    public int getEventCalls() {
        return eventCalls;
    }

    public java.util.Map<String, Object> getLastProfilePayload() {
        return lastProfilePayload;
    }

    public java.util.Map<String, Object> getLastEventPayload() {
        return lastEventPayload;
    }
}
