package com.ticketmicroservices.eventservice.service;

import com.ticketmicroservices.eventservice.document.EventLog;
import com.ticketmicroservices.eventservice.dto.CreateEventRequest;
import com.ticketmicroservices.eventservice.dto.EventDto;
import com.ticketmicroservices.eventservice.entity.Event;
import com.ticketmicroservices.eventservice.repository.EventRepository;
import com.ticketmicroservices.eventservice.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private EventLogRepository eventLogRepository;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public EventDto createEvent(CreateEventRequest request, Long userId) {
    Event event = new Event(
        request.getName(),
        request.getDescription(),
        request.getEventDate(),
        request.getVenue(),
        request.getTotalTickets(),
        request.getTicketPrice(),
        userId);

    event = eventRepository.save(event);

    // Log event creation
    EventLog log = new EventLog(event.getId(), "CREATED", "Event created by user " + userId, userId);
    eventLogRepository.save(log);

    // Send event created notification
    kafkaTemplate.send("event-notifications", "EVENT_CREATED", event.getId().toString());

    return convertToDto(event);
  }

  public List<EventDto> getActiveEvents() {
    List<Event> events = eventRepository.findByStatusAndEventDateAfter(
        Event.EventStatus.ACTIVE,
        LocalDateTime.now());
    return events.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<EventDto> getEventsByUser(Long userId) {
    List<Event> events = eventRepository.findByCreatedBy(userId);
    return events.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public EventDto getEventById(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Event not found"));
    return convertToDto(event);
  }

  public EventDto updateAvailableTickets(Long eventId, Integer ticketsSold) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("Event not found"));

    event.setAvailableTickets(event.getAvailableTickets() - ticketsSold);
    event = eventRepository.save(event);

    // Send ticket availability update
    kafkaTemplate.send("ticket-updates", "TICKETS_SOLD", eventId.toString());

    return convertToDto(event);
  }

  private EventDto convertToDto(Event event) {
    return new EventDto(
        event.getId(),
        event.getName(),
        event.getDescription(),
        event.getEventDate(),
        event.getVenue(),
        event.getTotalTickets(),
        event.getAvailableTickets(),
        event.getTicketPrice(),
        event.getCreatedBy(),
        event.getStatus().toString());
  }
}