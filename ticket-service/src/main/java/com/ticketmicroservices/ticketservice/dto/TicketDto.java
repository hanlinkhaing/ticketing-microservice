package com.ticketmicroservices.ticketservice.dto;

public class TicketDto {
  private Long id;
  private Long eventId;
  private Long userId;
  private Long orderId;
  private String seatNumber;
  private Double price;
  private String status;

  public TicketDto() {
  }

  public TicketDto(Long id, Long eventId, Long userId, Long orderId, String seatNumber, Double price, String status) {
    this.id = id;
    this.eventId = eventId;
    this.userId = userId;
    this.orderId = orderId;
    this.seatNumber = seatNumber;
    this.price = price;
    this.status = status;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public String getSeatNumber() {
    return seatNumber;
  }

  public void setSeatNumber(String seatNumber) {
    this.seatNumber = seatNumber;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}