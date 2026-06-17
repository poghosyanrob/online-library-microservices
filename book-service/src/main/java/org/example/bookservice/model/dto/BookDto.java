package org.example.bookservice.model.dto;

import java.io.Serializable;

public record BookDto(
        Long id,
        String title,
        String author,
        Double price,
        Integer quantity,
        String imageUrl
) implements Serializable {
}
