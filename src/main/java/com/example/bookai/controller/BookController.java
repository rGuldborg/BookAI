package com.example.bookai.controller;

import com.example.bookai.dto.BookDTO;
import com.example.bookai.service.BookService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Mono<List<BookDTO>> getBooks(@RequestParam(defaultValue = "books") String q) {
        return bookService.searchBooks(q);
    }

    @GetMapping("/{id}")
    public Mono<BookDTO> getBook(@PathVariable String id) {
        return bookService.getBookById(id);
    }
}
