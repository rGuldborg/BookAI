package com.example.bookai.dto;

public class BookDTO {
    private String id;
    private String title;
    private String imageUrl;
    private String author;
    private String publishedDate;
    private String description;

    public BookDTO(String id, String title, String imageUrl, String author, String publishedDate, String description) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.author = author;
        this.publishedDate = publishedDate;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getDescription() {
        return description;
    }
}
