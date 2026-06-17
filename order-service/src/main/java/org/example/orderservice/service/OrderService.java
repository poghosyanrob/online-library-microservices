package org.example.orderservice.service;

import org.example.orderservice.model.dto.BookDto;
import org.example.orderservice.model.dto.OrderDto;
import org.example.orderservice.model.dto.OrderRequest;
import org.example.orderservice.model.dto.OrderStatisticsDto;
import org.example.orderservice.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    List<OrderDto> getAllOrders();
    List<OrderDto> getMyOrders(String userEmail);
    OrderDto getOrderById(Long id);
    BookDto getBookFromBookService(Long bookId);
    OrderDto createOrder(OrderRequest request, String userEmail);
    OrderDto updateOrderStatus(Long id, OrderStatus status);
    OrderStatisticsDto getOrderStatistics();
}