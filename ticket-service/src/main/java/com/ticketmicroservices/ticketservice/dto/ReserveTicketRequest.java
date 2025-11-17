package com.ticketmicroservices.ticketservice.dto;

public class ReserveTicketRequest {
  private Long eventId;
  private Long userId;
  private Integer quantity;

  public ReserveTicketRequest() {
  }

  public ReserveTicketRequest(Long eventId, Long userId, Integer quantity) {
    this.eventId = eventId;
    this.userId = userId;
    this.quantity = quantity;
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

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}