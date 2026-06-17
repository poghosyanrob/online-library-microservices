package org.example.userservice.client;

import org.example.userservice.model.dto.OrderStatisticsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "order-service", url = "${order-service.url:http://order-service:8082}")
public interface OrderClient {

    @GetMapping("/api/orders/statistics")
    OrderStatisticsResponse getOrderStatistics();
}
