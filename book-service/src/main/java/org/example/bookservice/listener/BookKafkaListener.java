package org.example.bookservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookservice.event.OrderPlacedEvent;
import org.example.bookservice.model.Book;
import org.example.bookservice.service.impl.BookServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookKafkaListener {
    private final BookServiceImpl bookService;
    @KafkaListener(topics = "order-topic", groupId = "book-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order event from Kafka, Book ID: {}", event.getBookId());

        try {
            Book book = bookService.getBookEntityById(event.getBookId());
            int newQuantity = book.getQuantity() - event.getQuantity();
            if (newQuantity < 0) {
                log.warn("Insufficient stock for Book ID: {}", event.getBookId());
                return;
            }
            book.setQuantity(newQuantity);
            bookService.updateBookEntity(book);
            log.info("Book quantity updated successfully. New quantity: {}", newQuantity);
        } catch (RuntimeException e) {
            log.error("Book not found for ID: {}", event.getBookId());
        }
    }

}
