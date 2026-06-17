package org.example.orderservice.model.dto;

import org.example.orderservice.model.enums.OrderStatus;
import java.io.Serializable;

public record OrderDto(
        Long id,
        Long bookId,
        Integer quantity,
        OrderStatus status,
        String userEmail
) implements Serializable {
}