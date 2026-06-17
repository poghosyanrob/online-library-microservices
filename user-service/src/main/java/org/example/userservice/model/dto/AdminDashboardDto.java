package org.example.userservice.model.dto;

public record AdminDashboardDto(
        long totalBookTitles,
        long totalBookQuantity,
        long totalOrders,
        double totalRevenue
) {
}
