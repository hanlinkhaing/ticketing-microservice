package com.ticketmicroservices.eventservice.controller;

import com.ticketmicroservices.eventservice.dto.CreateEventRequest;
import com.ticketmicroservices.eventservice.dto.EventDto;
import com.ticketmicroservices.eventservice.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

  @Autowired
  private EventService eventService;

  @PostMapping
  public ResponseEntity<EventDto> createEvent(@RequestBody CreateEventRequest request,
      @RequestHeader("X-User-Id") Long userId) {
    EventDto event = eventService.createEvent(request, userId);
    return ResponseEntity.ok(event);
  }

  @GetMapping("/active")
  public ResponseEntity<List<EventDto>> getActiveEvents() {
    List<EventDto> events = eventService.getActiveEvents();
    return ResponseEntity.ok(events);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<EventDto>> getEventsByUser(@PathVariable Long userId) {
    List<EventDto> events = eventService.getEventsByUser(userId);
    return ResponseEntity.ok(events);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EventDto> getEventById(@PathVariable Long id) {
    EventDto event = eventService.getEventById(id);
    return ResponseEntity.ok(event);
  }

  @PutMapping("/{id}/tickets/{ticketsSold}")
  public ResponseEntity<EventDto> updateAvailableTickets(@PathVariable Long id,
      @PathVariable Integer ticketsSold) {
    EventDto event = eventService.updateAvailableTickets(id, ticketsSold);
    return ResponseEntity.ok(event);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Event Service is running");
  }
}