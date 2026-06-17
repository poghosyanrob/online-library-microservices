package org.example.bookservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Author must not be blank")
        @Size(max = 255, message = "Author must not exceed 255 characters")
        String author,

        @NotNull(message = "Price must not be null")
        @Positive(message = "Price must be positive")
        Double price,

        @NotNull(message = "Quantity must not be null")
        @PositiveOrZero(message = "Quantity must be zero or positive")
        Integer quantity
) {
}