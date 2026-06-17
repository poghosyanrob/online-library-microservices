package org.example.orderservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.client.BookClient;
import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.model.dto.BookDto;
import org.example.orderservice.event.OrderPlacedEvent;
import org.example.orderservice.model.BookOrder;
import org.example.orderservice.model.dto.OrderDto;
import org.example.orderservice.model.dto.OrderRequest;
import org.example.orderservice.model.dto.OrderStatisticsDto;
import org.example.orderservice.model.enums.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.service.OrderService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookClient bookClient;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final MessageSource messageSource;

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    @Cacheable(value = "orders")
    public List<OrderDto> getAllOrders() {
        log.info("Fetching all orders from PostgreSQL database");
        return orderMapper.toDtoList(orderRepository.findAll());
    }

    @Override
    @Cacheable(value = "order", key = "#id")
    public OrderDto getOrderById(Long id) {
        log.info("Fetching single order from PostgreSQL database, ID: {}", id);
        BookOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(msg("error.order.not.found")));
        return orderMapper.toDto(order);
    }

    @Override
    @Cacheable(value = "external_books", key = "#bookId")
    public BookDto getBookFromBookService(Long bookId) {
        log.info("order-service calling book-service via OpenFeign, ID: {}", bookId);
        return bookClient.getBookById(bookId);
    }

    @Override
    public List<OrderDto> getMyOrders(String userEmail) {
        return orderMapper.toDtoList(orderRepository.findByUserEmail(userEmail));
    }

    @Override
    @CacheEvict(value = "orders", allEntries = true)
    @Transactional
    public OrderDto createOrder(OrderRequest request, String userEmail) {
        String insufficientStockMsg = msg("error.order.insufficient.stock");
        String bookUnavailableMsg = msg("error.order.book.unavailable");

        try {
            BookDto book = getBookFromBookService(request.bookId());
            if (book.quantity() < request.quantity()) {
                throw new RuntimeException(insufficientStockMsg);
            }
            BookOrder order = orderMapper.toEntity(request);
            order.setStatus(OrderStatus.PENDING);
            order.setUserEmail(userEmail);

            BookOrder savedOrder = orderRepository.save(order);

            OrderPlacedEvent event = new OrderPlacedEvent(savedOrder.getBookId(), savedOrder.getQuantity());
            kafkaTemplate.send("order-topic", event);
            log.info("Kafka message sent successfully to order-topic");

            return orderMapper.toDto(savedOrder);

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().equals(insufficientStockMsg)) {
                throw e;
            }
            throw new RuntimeException(bookUnavailableMsg);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "order", key = "#id")
    })
    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus status) {
        BookOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(msg("error.order.not.found")));

        order.setStatus(status);
        BookOrder updatedOrder = orderRepository.save(order);
        return orderMapper.toDto(updatedOrder);
    }

    @Override
    public OrderStatisticsDto getOrderStatistics() {
        long totalOrders = orderRepository.countCompletedOrders();

        List<BookOrder> completedOrders = orderRepository.findAll().stream()
                .filter(order -> OrderStatus.COMPLETED == order.getStatus())
                .toList();

        double totalRevenue = 0.0;

        for (BookOrder order : completedOrders) {
            try {
                BookDto book = bookClient.getBookById(order.getBookId());
                if (book != null) {
                    totalRevenue += book.price() * order.getQuantity();
                }
            } catch (Exception e) {
                log.error("Failed to get book price for ID: " + order.getBookId(), e);
            }
        }

        return new OrderStatisticsDto(totalOrders, totalRevenue);
    }
}
