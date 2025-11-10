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
        String finalQuery = (query == null || query.isBlank()) ? "books" : query;

        Mono<GoogleBooksResponse> firstPage = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", finalQuery)
                        .queryParam("startIndex", 0)
                        .queryParam("maxResults", 10)
                        .queryParam("langRestrict", "en")
                        .queryParam("printType", "books")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class);

        Mono<GoogleBooksResponse> secondPage = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", finalQuery)
                        .queryParam("startIndex", 10)
                        .queryParam("maxResults", 10)
                        .queryParam("langRestrict", "en")
                        .queryParam("printType", "books")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class);

        return Mono.zip(firstPage, secondPage)
                .map(tuple -> {
                    List<Item> allItems = new java.util.ArrayList<>();
                    allItems.addAll(tuple.getT1().getItems());
                    allItems.addAll(tuple.getT2().getItems());

                    return allItems.stream()
                            .limit(12) // viser kun 12
                            .map(item -> new BookDTO(
                                    item.getId(),
                                    item.getVolumeInfo().getTitle(),
                                    item.getVolumeInfo().getImageLinks() != null
                                            ? item.getVolumeInfo().getImageLinks().getThumbnail()
                                            : ""
                            ))
                            .collect(Collectors.toList());
                });
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

    // === Response DTO'er til Google Books ===
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
