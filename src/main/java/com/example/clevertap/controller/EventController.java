package com.example.clevertap.controller;

import com.example.clevertap.dto.EventRequest;
import com.example.clevertap.model.Event;
import com.example.clevertap.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) { this.eventService = eventService; }

    @PostMapping("/api/events")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest req) {
        Event e = eventService.createEvent(req);
        return ResponseEntity.ok(e);
    }

    @GetMapping("/api/users/{userId}/events")
    public ResponseEntity<List<Event>> getEvents(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getEventsForUser(userId));
    }
}
