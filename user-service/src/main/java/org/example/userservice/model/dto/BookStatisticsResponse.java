package org.example.userservice.model.dto;

public record BookStatisticsResponse(
        long totalBookTitles,
        long totalBookQuantity
) {
}
