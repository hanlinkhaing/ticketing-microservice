package com.ticketmicroservices.eventservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "event_date", nullable = false)
  private LocalDateTime eventDate;

  @Column(nullable = false)
  private String venue;

  @Column(name = "total_tickets", nullable = false)
  private Integer totalTickets;

  @Column(name = "available_tickets", nullable = false)
  private Integer availableTickets;

  @Column(name = "ticket_price", nullable = false)
  private Double ticketPrice;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  private EventStatus status;

  public enum EventStatus {
    ACTIVE, INACTIVE, CANCELLED
  }

  // Constructors
  public Event() {
  }

  public Event(String name, String description, LocalDateTime eventDate, String venue,
      Integer totalTickets, Double ticketPrice, Long createdBy) {
    this.name = name;
    this.description = description;
    this.eventDate = eventDate;
    this.venue = venue;
    this.totalTickets = totalTickets;
    this.availableTickets = totalTickets;
    this.ticketPrice = ticketPrice;
    this.createdBy = createdBy;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.status = EventStatus.ACTIVE;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public EventStatus getStatus() {
    return status;
  }

  public void setStatus(EventStatus status) {
    this.status = status;
  }
}