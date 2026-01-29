package com.example.clevertap.service;

import com.example.clevertap.client.CleverTapClient;
import com.example.clevertap.dto.EventRequest;
import com.example.clevertap.model.Event;
import com.example.clevertap.model.User;
import com.example.clevertap.repository.EventRepository;
import com.example.clevertap.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class EventServiceUnitTest {
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private CleverTapClient client;
    private EventService eventService;

    @BeforeEach
    void setup() {
        eventRepository = Mockito.mock(EventRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        client = new TestCleverTapClient();
        eventService = new EventService(eventRepository, userRepository, client);
    }

    @Test
    void createEvent_userNotFound_throws() {
        EventRequest req = new EventRequest();
        req.setUserId(99L);
        req.setEventName("evt");
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class, () -> eventService.createEvent(req));
    }

    @Test
    void createEvent_success_persistsAndPushes() {
        User u = new User(); u.setId(1L); u.setIdentity("id-X"); u.setEmail("a@b.com");
        EventRequest req = new EventRequest(); req.setUserId(1L); req.setEventName("signup");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(eventRepository.save(Mockito.any(Event.class))).thenAnswer(i -> {
            Event e = i.getArgument(0);
            e.setId(10L);
            return e;
        });

        Event saved = eventService.createEvent(req);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals(10L, saved.getId());
        TestCleverTapClient stub = (TestCleverTapClient) client;
        Assertions.assertEquals(1, stub.getEventCalls());
        Assertions.assertNotNull(stub.getLastEventPayload());
    }
}
