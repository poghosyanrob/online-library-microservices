package org.example.orderservice.client;

import org.example.orderservice.model.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "book-service", url = "${book-service.url}", configuration = FeignClientConfig.class)
public interface BookClient {
    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable Long id);

    @PutMapping("/api/books/{id}")
    BookDto updateBook(@PathVariable Long id, @RequestBody BookDto book);
}
