package org.example.bookservice.model.dto;

public record BookStatisticsDto(
        long totalBookTitles,
        long totalBookQuantity
) {
}
