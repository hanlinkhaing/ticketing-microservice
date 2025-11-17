package com.ticketmicroservices.eventservice.repository;

import com.ticketmicroservices.eventservice.document.EventLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, String> {
  List<EventLog> findByEventId(Long eventId);
}