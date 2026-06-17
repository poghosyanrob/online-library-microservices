package org.example.orderservice.model.dto;

public record OrderStatisticsDto(
        long totalOrders,
        double totalRevenue
) {
}
