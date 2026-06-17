package org.example.userservice.model.dto;

public record OrderStatisticsResponse(
        long totalOrders,
        double totalRevenue
) {}
