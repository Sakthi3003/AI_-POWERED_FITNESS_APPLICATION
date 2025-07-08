package com.fitness.aiservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    // For internal service calls (Eureka/Service Discovery)
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    // For calling internal microservices like USERSERVICE
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .baseUrl("http://USERSERVICE")
                .build();
    }

    // For calling external services like Gemini API
    @Bean
    public WebClient externalWebClient() {
        return WebClient.builder().build(); // No @LoadBalanced
    }
}
