package org.example.bookservice.repository;

import org.example.bookservice.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    @Query("SELECT COUNT(b) FROM Book b")
    long countTotalTitles();

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b")
    long sumTotalQuantity();
}
