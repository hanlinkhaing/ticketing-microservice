package com.ticketmicroservices.orderservice.service;

import com.ticketmicroservices.orderservice.dto.CreateOrderRequest;
import com.ticketmicroservices.orderservice.dto.OrderDto;
import com.ticketmicroservices.orderservice.entity.Order;
import com.ticketmicroservices.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public OrderDto createOrder(CreateOrderRequest request) {
    Order order = new Order(
        request.getUserId(),
        request.getEventId(),
        request.getTotalAmount(),
        request.getTicketQuantity());

    order = orderRepository.save(order);

    // Send order created event
    kafkaTemplate.send("order-events", "ORDER_CREATED", order.getId().toString());

    return convertToDto(order);
  }

  public OrderDto getOrderById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order not found"));
    return convertToDto(order);
  }

  public List<OrderDto> getOrdersByUser(Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);
    return orders.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public OrderDto confirmOrder(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    order.setStatus(Order.OrderStatus.CONFIRMED);
    order = orderRepository.save(order);

    // Send order confirmed event
    kafkaTemplate.send("order-events", "ORDER_CONFIRMED", order.getId().toString());

    return convertToDto(order);
  }

  public OrderDto cancelOrder(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    order.setStatus(Order.OrderStatus.CANCELLED);
    order = orderRepository.save(order);

    // Send order cancelled event
    kafkaTemplate.send("order-events", "ORDER_CANCELLED", order.getId().toString());

    return convertToDto(order);
  }

  private OrderDto convertToDto(Order order) {
    return new OrderDto(
        order.getId(),
        order.getUserId(),
        order.getEventId(),
        order.getTotalAmount(),
        order.getTicketQuantity(),
        order.getStatus().toString(),
        order.getCreatedAt().toString());
  }
}