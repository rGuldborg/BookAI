package com.example.bookai.service;

import com.example.bookai.dto.ChatRequestDTO;
import com.example.bookai.dto.ChatResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final String model;

    public OpenAIService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.model}") String model
    ) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    public Mono<ChatResponseDTO> askAboutBook(String bookTitle, String question) {

        ChatRequestDTO request = new ChatRequestDTO(
                model,
                new ChatRequestDTO.Message[]{
                        new ChatRequestDTO.Message("system", "Du er en hjælpsom assistent, der kun må tale om bogen \"" + bookTitle + "\"."),
                        new ChatRequestDTO.Message("user", question)
                }
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponseDTO.class);
    }
}
