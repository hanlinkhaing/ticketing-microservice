package com.ticketmicroservices.orderservice.controller;

import com.ticketmicroservices.orderservice.dto.CreateOrderRequest;
import com.ticketmicroservices.orderservice.dto.OrderDto;
import com.ticketmicroservices.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
    OrderDto order = orderService.createOrder(request);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
    OrderDto order = orderService.getOrderById(id);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<OrderDto>> getOrdersByUser(@PathVariable Long userId) {
    List<OrderDto> orders = orderService.getOrdersByUser(userId);
    return ResponseEntity.ok(orders);
  }

  @PutMapping("/{id}/confirm")
  public ResponseEntity<OrderDto> confirmOrder(@PathVariable Long id) {
    OrderDto order = orderService.confirmOrder(id);
    return ResponseEntity.ok(order);
  }

  @PutMapping("/{id}/cancel")
  public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id) {
    OrderDto order = orderService.cancelOrder(id);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Order Service is running");
  }
}