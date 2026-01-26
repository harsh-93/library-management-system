package com.health.spry.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("book-service", r -> r
                        .path("/api/books/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("bookServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/book-service")
                                )
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                                        .setBackoff(
                                                java.time.Duration.ofMillis(100),
                                                java.time.Duration.ofMillis(500),
                                                2,
                                                true
                                        )
                                )
                        )
                        .uri("lb://BOOK-SERVICE")
                )
                .route("login-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("loginServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/login-service")
                                )
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                                        .setBackoff(
                                                java.time.Duration.ofMillis(100),
                                                java.time.Duration.ofMillis(500),
                                                2,
                                                true
                                        )
                                )
                        )
                        .uri("lb://LOGIN-SERVICE")
                )
                .build();
    }
}