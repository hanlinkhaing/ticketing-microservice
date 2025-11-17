package com.ticketmicroservices.eventservice.dto;

import java.time.LocalDateTime;

public class CreateEventRequest {
  private String name;
  private String description;
  private LocalDateTime eventDate;
  private String venue;
  private Integer totalTickets;
  private Double ticketPrice;

  public CreateEventRequest() {
  }

  public CreateEventRequest(String name, String description, LocalDateTime eventDate, String venue,
      Integer totalTickets, Double ticketPrice) {
    this.name = name;
    this.description = description;
    this.eventDate = eventDate;
    this.venue = venue;
    this.totalTickets = totalTickets;
    this.ticketPrice = ticketPrice;
  }

  // Getters and Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getEventDate() {
    return eventDate;
  }

  public void setEventDate(LocalDateTime eventDate) {
    this.eventDate = eventDate;
  }

  public String getVenue() {
    return venue;
  }

  public void setVenue(String venue) {
    this.venue = venue;
  }

  public Integer getTotalTickets() {
    return totalTickets;
  }

  public void setTotalTickets(Integer totalTickets) {
    this.totalTickets = totalTickets;
  }

  public Double getTicketPrice() {
    return ticketPrice;
  }

  public void setTicketPrice(Double ticketPrice) {
    this.ticketPrice = ticketPrice;
  }
}