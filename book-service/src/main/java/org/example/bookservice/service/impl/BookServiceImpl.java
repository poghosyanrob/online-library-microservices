package org.example.bookservice.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookservice.mapper.BookMapper;
import org.example.bookservice.model.Book;
import org.example.bookservice.model.dto.BookDto;
import org.example.bookservice.model.dto.BookRequest;
import org.example.bookservice.model.dto.BookStatisticsDto;
import org.example.bookservice.repository.BookRepository;
import org.example.bookservice.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final MessageSource messageSource;

    @Value("${app.upload.dir:uploads/books}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadDir, e);
        }
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    @Cacheable(value = "books")
    public List<BookDto> getAllBooks() {
        log.info("Fetching all books from PostgreSQL database");
        return bookMapper.toDtoList(bookRepository.findAll());
    }

    @Override
    public List<BookDto> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookMapper.toDtoList(
                bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query.trim(), query.trim())
        );
    }

    @Override
    @Cacheable(value = "book", key = "#id")
    public BookDto getBookById(Long id) {
        log.info("Fetching single book from PostgreSQL database, ID: {}", id);
        Book book = getBookEntityById(id);
        return bookMapper.toDto(book);
    }

    @Override
    public Book getBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(msg("error.book.not.found")));
    }

    @Override
    @CacheEvict(value = "books", allEntries = true)
    @Transactional
    public BookDto createBook(BookRequest request) {
        Book book = bookMapper.toEntity(request);
        Book savedBook = bookRepository.save(book);
        return bookMapper.toDto(savedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#id")
    })
    @Transactional
    public BookDto updateBook(Long id, BookRequest request) {
        Book existingBook = getBookEntityById(id);
        bookMapper.updateEntityFromRequest(request, existingBook);
        Book updatedBook = bookRepository.save(existingBook);
        return bookMapper.toDto(updatedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#book.id")
    })
    @Transactional
    public void updateBookEntity(Book book) {
        bookRepository.save(book);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#id")
    })
    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookEntityById(id);
        bookRepository.delete(book);
    }

    @Override
    public BookStatisticsDto getBookStatistics() {
        long totalTitles = bookRepository.countTotalTitles();
        long totalQuantity = bookRepository.sumTotalQuantity();
        return new BookStatisticsDto(totalTitles, totalQuantity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#id")
    })
    @Transactional
    public BookDto uploadBookImage(Long id, MultipartFile file) {
        Book book = getBookEntityById(id);
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;
        Path targetPath = Paths.get(uploadDir).resolve(filename);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
        book.setImageUrl("/api/books/images/" + filename);
        Book saved = bookRepository.save(book);
        return bookMapper.toDto(saved);
    }
}
