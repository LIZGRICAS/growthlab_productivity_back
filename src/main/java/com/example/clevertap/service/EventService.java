package com.example.clevertap.service;

import com.example.clevertap.client.CleverTapClient;
import com.example.clevertap.dto.EventRequest;
import com.example.clevertap.exception.ResourceNotFoundException;
import com.example.clevertap.model.Event;
import com.example.clevertap.model.User;
import com.example.clevertap.repository.EventRepository;
import com.example.clevertap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CleverTapClient clevertap;

    public EventService(EventRepository eventRepository, UserRepository userRepository, CleverTapClient clevertap) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.clevertap = clevertap;
    }

    public Event createEvent(EventRequest req) {
        User user = userRepository.findById(req.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Event e = new Event();
        e.setUser(user);
        e.setEventName(req.getEventName());
        e.setPropertiesJson(req.getPropertiesJson());
        Event saved = eventRepository.save(e);

        Map<String, Object> eventPayload = new HashMap<>();
        Map<String, Object> evt = new HashMap<>();
        evt.put("evtName", saved.getEventName());
        evt.put("identity", user.getIdentity());
        evt.put("ts", System.currentTimeMillis());
        eventPayload.put("d", new Object[]{evt});
        try { clevertap.uploadEvent(eventPayload); } catch (Exception ignored) {}

        return saved;
    }

    public List<Event> getEventsForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return eventRepository.findByUser(user);
    }
}
