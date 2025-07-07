package com.fitness.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        try {
            return userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block(); // blocks the reactive response and waits for the result
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("User not found");
            } else {
                throw new RuntimeException("Error validating user: " + ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occurred", ex);
        }

    }
}
