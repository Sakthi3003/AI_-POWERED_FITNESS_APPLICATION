package com.fitness.aiservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    private final WebClient webClient;

    @Value("${GEMINI_API_URL}")
    private String geminiApiUrl;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    @Autowired
    public GeminiService(@Qualifier("externalWebClient") WebClient externalWebClient) {
        this.webClient = externalWebClient;
    }

    public String getAnswer(String question) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", question)
                                )
                        )
                )
        );

        try {
            return webClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Gemini API error: " + error)))
                    )
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error occurred: " + ex.getMessage();
        }
    }
}
