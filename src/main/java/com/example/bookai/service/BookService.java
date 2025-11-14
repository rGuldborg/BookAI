package com.example.bookai.service;

import com.example.bookai.dto.BookDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
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
        String finalQuery;

        if (query == null || query.isBlank() || query.equalsIgnoreCase("books")) {
            finalQuery = "harry potter OR hunger games OR lord of the rings OR davinci mystery";
        } else {
            finalQuery = query;
        }

        Mono<GoogleBooksResponse> page1 = fetchPage(finalQuery, 0)
                .onErrorReturn(new GoogleBooksResponse());

        Mono<GoogleBooksResponse> page2 = fetchPage(finalQuery, 10)
                .onErrorResume(err -> fetchPage(finalQuery, 20)
                        .onErrorReturn(new GoogleBooksResponse()));

        return Mono.zip(page1, page2)
                .map(tuple -> {
                    List<Item> all = new java.util.ArrayList<>();
                    all.addAll(tuple.getT1().getItems());
                    all.addAll(tuple.getT2().getItems());

                    return all.stream()
                            .limit(12)
                            .map(item -> {
                                VolumeInfo info = item.getVolumeInfo();
                                return new BookDTO(
                                        item.getId(),
                                        info.getTitle() != null ? info.getTitle() : "Unknown title",
                                        info.getImageLinks() != null && info.getImageLinks().getThumbnail() != null
                                                ? info.getImageLinks().getThumbnail()
                                                : "",
                                        info.getAuthors() != null && !info.getAuthors().isEmpty()
                                                ? String.join(", ", info.getAuthors())
                                                : "Unknown author",
                                        info.getPublishedDate() != null ? info.getPublishedDate() : "Unknown",
                                        info.getDescription() != null ? info.getDescription() : "No description available"
                                );
                            })
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
                .map(book -> {
                    VolumeInfo info = book.getVolumeInfo();
                    return new BookDTO(
                            book.getId(),
                            info.getTitle() != null ? info.getTitle() : "Unknown title",
                            info.getImageLinks() != null && info.getImageLinks().getThumbnail() != null
                                    ? info.getImageLinks().getThumbnail()
                                    : "",
                            info.getAuthors() != null && !info.getAuthors().isEmpty()
                                    ? String.join(", ", info.getAuthors())
                                    : "Unknown author",
                            info.getPublishedDate() != null ? info.getPublishedDate() : "Unknown",
                            info.getDescription() != null ? info.getDescription() : "No description available"
                    );
                });
    }

    private Mono<GoogleBooksResponse> fetchPage(String query, int startIndex) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", query)
                        .queryParam("startIndex", startIndex)
                        .queryParam("maxResults", 10)
                        .queryParam("langRestrict", "en")
                        .queryParam("printType", "books")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class);
    }

    private static class GoogleBooksResponse {
        private List<Item> items;
        public List<Item> getItems() { return items == null ? List.of() : items; }
    }

    private static class GoogleBookDetail {
        private String id;
        private VolumeInfo volumeInfo;
        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    private static class Item {
        private String id;
        private VolumeInfo volumeInfo;
        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
    }

    private static class VolumeInfo {
        private String title;
        private List<String> authors;
        private String publishedDate;
        private String description;
        private ImageLinks imageLinks;

        public String getTitle() { return title; }
        public List<String> getAuthors() { return authors; }
        public String getPublishedDate() { return publishedDate; }
        public String getDescription() { return description; }
        public ImageLinks getImageLinks() { return imageLinks; }
    }

    private static class ImageLinks {
        private String thumbnail;
        public String getThumbnail() { return thumbnail; }
    }
}
