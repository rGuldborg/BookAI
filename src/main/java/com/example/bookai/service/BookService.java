package com.example.bookai.service;

import com.example.bookai.dto.BookDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final WebClient webClient;
    private final String apiKey;

    public BookService(@Value("${google.books.api.key}") String apiKey) {
        this.webClient = WebClient.create("https://www.googleapis.com/books/v1");
        this.apiKey = apiKey;
    }

    public Mono<List<BookDTO>> searchBooks(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", query)
                        .queryParam("maxResults", 20)
                        .queryParam("langRestrict", "en","dk")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class)
                .map(response -> response.getItems().stream()
                        .map(item -> new BookDTO(
                                item.getId(),
                                item.getVolumeInfo().getTitle(),
                                item.getVolumeInfo().getImageLinks() != null
                                        ? item.getVolumeInfo().getImageLinks().getThumbnail()
                                        : ""
                        ))
                        .collect(Collectors.toList()));
    }

    public Mono<BookDTO> getBookById(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes/{id}")
                        .queryParam("key", apiKey)
                        .build(id))
                .retrieve()
                .bodyToMono(GoogleBookDetail.class)
                .map(book -> new BookDTO(
                        book.getId(),
                        book.getVolumeInfo().getTitle(),
                        book.getVolumeInfo().getImageLinks() != null
                                ? book.getVolumeInfo().getImageLinks().getThumbnail()
                                : ""
                ));
    }

    private static class GoogleBooksResponse {
        private List<Item> items;
        public List<Item> getItems() { return items == null ? List.of() : items; }
    }

    private static class Item {
        private String id;
        private VolumeInfo volumeInfo;
        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    private static class GoogleBookDetail {
        private String id;
        private VolumeInfo volumeInfo;
        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    private static class VolumeInfo {
        private String title;
        private ImageLinks imageLinks;
        public String getTitle() { return title; }
        public ImageLinks getImageLinks() { return imageLinks; }
    }

    private static class ImageLinks {
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
