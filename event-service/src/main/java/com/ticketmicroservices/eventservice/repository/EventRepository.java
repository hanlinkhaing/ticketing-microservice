package com.ticketmicroservices.eventservice.repository;

import com.ticketmicroservices.eventservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
  List<Event> findByStatusAndEventDateAfter(Event.EventStatus status, LocalDateTime date);

  List<Event> findByCreatedBy(Long userId);
}