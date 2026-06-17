package org.example.orderservice.repository;

import org.example.orderservice.model.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<BookOrder, Long> {
    @Query("SELECT COUNT(o) FROM BookOrder o WHERE o.status = org.example.orderservice.model.enums.OrderStatus.COMPLETED")
    long countCompletedOrders();

    List<BookOrder> findByUserEmail(String userEmail);
}
