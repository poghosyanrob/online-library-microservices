package org.example.userservice.client;

import org.example.userservice.model.dto.BookStatisticsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "book-service", url = "${book-service.url:http://book-service:8081}")
public interface BookClient {

    @GetMapping("/api/books/statistics")
    BookStatisticsResponse getBookStatistics();
}
