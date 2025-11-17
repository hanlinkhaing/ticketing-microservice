package com.ticketmicroservices.ticketservice.service;

import com.ticketmicroservices.ticketservice.dto.ReserveTicketRequest;
import com.ticketmicroservices.ticketservice.dto.TicketDto;
import com.ticketmicroservices.ticketservice.entity.Ticket;
import com.ticketmicroservices.ticketservice.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

  @Autowired
  private TicketRepository ticketRepository;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Transactional
  public List<TicketDto> reserveTickets(ReserveTicketRequest request, Long orderId) {
    // Check if enough tickets are available
    long availableCount = ticketRepository.countByEventIdAndStatus(request.getEventId(), Ticket.TicketStatus.AVAILABLE);

    if (availableCount < request.getQuantity()) {
      throw new RuntimeException("Not enough tickets available");
    }

    // Reserve tickets
    List<Ticket> availableTickets = ticketRepository.findByEventIdAndStatus(
        request.getEventId(),
        Ticket.TicketStatus.AVAILABLE);

    List<Ticket> reservedTickets = availableTickets.stream()
        .limit(request.getQuantity())
        .map(ticket -> {
          ticket.setStatus(Ticket.TicketStatus.RESERVED);
          ticket.setUserId(request.getUserId());
          ticket.setOrderId(orderId);
          return ticket;
        })
        .collect(Collectors.toList());

    reservedTickets = ticketRepository.saveAll(reservedTickets);

    // Send ticket reservation event
    kafkaTemplate.send("ticket-events", "TICKETS_RESERVED", orderId.toString());

    return reservedTickets.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional
  public List<TicketDto> confirmTickets(Long orderId) {
    List<Ticket> reservedTickets = ticketRepository.findByOrderId(orderId);

    reservedTickets.forEach(ticket -> {
      ticket.setStatus(Ticket.TicketStatus.SOLD);
    });

    reservedTickets = ticketRepository.saveAll(reservedTickets);

    // Send ticket confirmation event
    kafkaTemplate.send("ticket-events", "TICKETS_SOLD", orderId.toString());

    return reservedTickets.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional
  public void cancelTickets(Long orderId) {
    List<Ticket> reservedTickets = ticketRepository.findByOrderId(orderId);

    reservedTickets.forEach(ticket -> {
      ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
      ticket.setUserId(null);
      ticket.setOrderId(null);
    });

    ticketRepository.saveAll(reservedTickets);

    // Send ticket cancellation event
    kafkaTemplate.send("ticket-events", "TICKETS_CANCELLED", orderId.toString());
  }

  public List<TicketDto> getTicketsByUser(Long userId) {
    List<Ticket> tickets = ticketRepository.findByUserId(userId);
    return tickets.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<TicketDto> getTicketsByOrder(Long orderId) {
    List<Ticket> tickets = ticketRepository.findByOrderId(orderId);
    return tickets.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  private TicketDto convertToDto(Ticket ticket) {
    return new TicketDto(
        ticket.getId(),
        ticket.getEventId(),
        ticket.getUserId(),
        ticket.getOrderId(),
        ticket.getSeatNumber(),
        ticket.getPrice(),
        ticket.getStatus().toString());
  }
}