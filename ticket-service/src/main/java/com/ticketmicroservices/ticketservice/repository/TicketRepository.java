package com.ticketmicroservices.ticketservice.repository;

import com.ticketmicroservices.ticketservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
  List<Ticket> findByEventIdAndStatus(Long eventId, Ticket.TicketStatus status);

  List<Ticket> findByUserId(Long userId);

  List<Ticket> findByOrderId(Long orderId);

  long countByEventIdAndStatus(Long eventId, Ticket.TicketStatus status);
}