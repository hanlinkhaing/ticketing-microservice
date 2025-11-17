package com.ticketmicroservices.orderservice.dto;

public class OrderDto {
  private Long id;
  private Long userId;
  private Long eventId;
  private Double totalAmount;
  private Integer ticketQuantity;
  private String status;
  private String createdAt;

  public OrderDto() {
  }

  public OrderDto(Long id, Long userId, Long eventId, Double totalAmount, Integer ticketQuantity, String status,
      String createdAt) {
    this.id = id;
    this.userId = userId;
    this.eventId = eventId;
    this.totalAmount = totalAmount;
    this.ticketQuantity = ticketQuantity;
    this.status = status;
    this.createdAt = createdAt;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}