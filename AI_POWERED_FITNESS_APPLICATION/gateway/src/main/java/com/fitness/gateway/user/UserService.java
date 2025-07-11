package com.fitness.gateway.user;


import com.fitness.gateway.user.model.RegisterRequest;
import com.fitness.gateway.user.model.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class  UserService {
    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId) {
        try {
            return userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        if(ex.getStatusCode() == HttpStatus.NOT_FOUND)
                            return Mono.error(new RuntimeException("User not found"));
                        else if (ex.getStatusCode() == HttpStatus.BAD_REQUEST)
                            return Mono.error(new RuntimeException("InvalidRequest"));
                        return Mono.error(new RuntimeException("User not found"));
                    });

        }catch(Exception e){
            return Mono.error(new RuntimeException("User not found"));
        }

    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {
        log.info("Calling user registration api for email");
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if(ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                        return Mono.error(new RuntimeException("Internal server error"));
                    else if (ex.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("Invalid Request"));
                    return Mono.error(new RuntimeException("User not found"));
                });
    }
}
