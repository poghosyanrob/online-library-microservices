package org.example.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
        @NotNull(message = "Book ID must not be null")
        @Positive(message = "Book ID must be positive")
        Long bookId,

        @NotNull(message = "Quantity must not be null")
        @Positive(message = "Quantity must be positive")
        Integer quantity
) {
}