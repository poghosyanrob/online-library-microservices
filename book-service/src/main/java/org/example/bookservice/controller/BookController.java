package org.example.bookservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bookservice.model.dto.BookDto;
import org.example.bookservice.model.dto.BookRequest;
import org.example.bookservice.model.dto.BookStatisticsDto;
import org.example.bookservice.service.impl.BookServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookServiceImpl bookService;

    @Value("${app.upload.dir:uploads/books}")
    private String uploadDir;


    @GetMapping
    public List<BookDto> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/search")
    public List<BookDto> searchBooks(@RequestParam(defaultValue = "") String q) {
        return bookService.searchBooks(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookService.getBookById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookRequest request) {
        BookDto savedBook = bookService.createBook(request);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        try {
            return ResponseEntity.ok(bookService.updateBook(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<BookStatisticsDto> getBookStatistics() {
        return ResponseEntity.ok(bookService.getBookStatistics());
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<BookDto> uploadBookImage(@PathVariable Long id,
                                                   @RequestParam("file") MultipartFile file) {
        try {
            BookDto dto = bookService.uploadBookImage(id, file);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getBookImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg";
                if (filename.endsWith(".png")) contentType = "image/png";
                else if (filename.endsWith(".gif")) contentType = "image/gif";
                else if (filename.endsWith(".webp")) contentType = "image/webp";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}