package org.example.orderservice.model.dto;

import java.io.Serializable;

public record BookDto(
        Long id,
        String title,
        String author,
        Double price,
        Integer quantity
) implements Serializable {

    private static final long serialVersionUID = 1L;
}