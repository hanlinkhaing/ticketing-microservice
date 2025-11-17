package com.ticketmicroservices.orderservice.dto;

public class CreateOrderRequest {
  private Long userId;
  private Long eventId;
  private Integer ticketQuantity;
  private Double totalAmount;

  public CreateOrderRequest() {
  }

  public CreateOrderRequest(Long userId, Long eventId, Integer ticketQuantity, Double totalAmount) {
    this.userId = userId;
    this.eventId = eventId;
    this.ticketQuantity = ticketQuantity;
    this.totalAmount = totalAmount;
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

  public Integer getTicketQuantity() {
    return ticketQuantity;
  }

  public void setTicketQuantity(Integer ticketQuantity) {
    this.ticketQuantity = ticketQuantity;
  }

  public Double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Double totalAmount) {
    this.totalAmount = totalAmount;
  }
}