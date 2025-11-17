package com.ticketmicroservices.eventservice.dto;

import java.time.LocalDateTime;

public class EventDto {
  private Long id;
  private String name;
  private String description;
  private LocalDateTime eventDate;
  private String venue;
  private Integer totalTickets;
  private Integer availableTickets;
  private Double ticketPrice;
  private Long createdBy;
  private String status;

  public EventDto() {
  }

  public EventDto(Long id, String name, String description, LocalDateTime eventDate, String venue,
      Integer totalTickets, Integer availableTickets, Double ticketPrice, Long createdBy, String status) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.eventDate = eventDate;
    this.venue = venue;
    this.totalTickets = totalTickets;
    this.availableTickets = availableTickets;
    this.ticketPrice = ticketPrice;
    this.createdBy = createdBy;
    this.status = status;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Integer getAvailableTickets() {
    return availableTickets;
  }

  public void setAvailableTickets(Integer availableTickets) {
    this.availableTickets = availableTickets;
  }

  public Double getTicketPrice() {
    return ticketPrice;
  }

  public void setTicketPrice(Double ticketPrice) {
    this.ticketPrice = ticketPrice;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}