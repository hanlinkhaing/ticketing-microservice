package com.ticketmicroservices.ticketservice.controller;

import com.ticketmicroservices.ticketservice.dto.ReserveTicketRequest;
import com.ticketmicroservices.ticketservice.dto.TicketDto;
import com.ticketmicroservices.ticketservice.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

  @Autowired
  private TicketService ticketService;

  @PostMapping("/reserve")
  public ResponseEntity<List<TicketDto>> reserveTickets(@RequestBody ReserveTicketRequest request,
      @RequestParam Long orderId) {
    List<TicketDto> tickets = ticketService.reserveTickets(request, orderId);
    return ResponseEntity.ok(tickets);
  }

  @PutMapping("/confirm/{orderId}")
  public ResponseEntity<List<TicketDto>> confirmTickets(@PathVariable Long orderId) {
    List<TicketDto> tickets = ticketService.confirmTickets(orderId);
    return ResponseEntity.ok(tickets);
  }

  @PutMapping("/cancel/{orderId}")
  public ResponseEntity<Void> cancelTickets(@PathVariable Long orderId) {
    ticketService.cancelTickets(orderId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<TicketDto>> getTicketsByUser(@PathVariable Long userId) {
    List<TicketDto> tickets = ticketService.getTicketsByUser(userId);
    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<List<TicketDto>> getTicketsByOrder(@PathVariable Long orderId) {
    List<TicketDto> tickets = ticketService.getTicketsByOrder(orderId);
    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Ticket Service is running");
  }
}