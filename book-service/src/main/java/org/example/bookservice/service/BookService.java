package org.example.bookservice.service;

import org.example.bookservice.model.Book;
import org.example.bookservice.model.dto.BookDto;
import org.example.bookservice.model.dto.BookRequest;
import org.example.bookservice.model.dto.BookStatisticsDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    List<BookDto> getAllBooks();
    List<BookDto> searchBooks(String query);
    BookDto getBookById(Long id);
    Book getBookEntityById(Long id);
    BookDto createBook(BookRequest request);
    BookDto updateBook(Long id, BookRequest request);
    void updateBookEntity(Book book);
    void deleteBook(Long id);
    BookStatisticsDto getBookStatistics();
    BookDto uploadBookImage(Long id, MultipartFile file);
}
