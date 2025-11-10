package com.example.bookai.controller;

import com.example.bookai.dto.ChatResponseDTO;
import com.example.bookai.service.OpenAIService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OpenAIService openAIService;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping
    public Mono<ChatResponseDTO> chat(@RequestParam String book, @RequestParam String question) {
        return openAIService.askAboutBook(book, question);
    }
}
