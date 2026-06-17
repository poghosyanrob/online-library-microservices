package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.dto.OrderDto;
import org.example.orderservice.model.dto.OrderRequest;
import org.example.orderservice.model.dto.OrderStatisticsDto;
import org.example.orderservice.model.enums.OrderStatus;
import org.example.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/my")
    public List<OrderDto> getMyOrders(@RequestHeader("X-User-Email") String userEmail) {
        return orderService.getMyOrders(userEmail);
    }

    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsDto> getOrderStatistics() {
        return ResponseEntity.ok(orderService.getOrderStatistics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            OrderDto savedOrder = orderService.createOrder(request, userEmail);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
