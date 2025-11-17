package com.ticketmicroservices.orderservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "total_amount", nullable = false)
  private Double totalAmount;

  @Column(name = "ticket_quantity", nullable = false)
  private Integer ticketQuantity;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public enum OrderStatus {
    PENDING, CONFIRMED, CANCELLED, FAILED
  }

  // Constructors
  public Order() {
  }

  public Order(Long userId, Long eventId, Double totalAmount, Integer ticketQuantity) {
    this.userId = userId;
    this.eventId = eventId;
    this.totalAmount = totalAmount;
    this.ticketQuantity = ticketQuantity;
    this.status = OrderStatus.PENDING;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public Double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Double totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Integer getTicketQuantity() {
    return ticketQuantity;
  }

  public void setTicketQuantity(Integer ticketQuantity) {
    this.ticketQuantity = ticketQuantity;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
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
}