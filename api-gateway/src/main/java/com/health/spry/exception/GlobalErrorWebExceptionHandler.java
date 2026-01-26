package com.health.spry.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred";

        log.error("Error in gateway: Type={}, Message={}", ex.getClass().getName(), ex.getMessage());

        // Handle NotFoundException (Service not found in Eureka)
        if (ex instanceof NotFoundException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = extractServiceNameAndFormat(ex.getMessage());
            log.error("Service not found in Eureka: {}", ex.getMessage());
        }
        // Handle TimeoutException
        else if (ex instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Service request timed out";
            log.error("Service timeout: {}", ex.getMessage());
        }
        // Handle ResponseStatusException
        else if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = (HttpStatus) rse.getStatusCode();
            
            
            if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.NOT_FOUND) {
                message = "The requested service is currently unavailable. Please try again later." +ex.getMessage() ;
            } else {
                message = rse.getReason() != null ? rse.getReason() : "Gateway error";
            }
            log.error("Response status exception: Status={}, Message={}", status, ex.getMessage());
        }
        // Handle ConnectException and other connection errors
        else if (ex.getCause() instanceof java.net.ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Unable to connect to the service. Please try again later.";
            log.error("Connection error: {}", ex.getMessage());
        }
        // Handle other exceptions
        else {
            log.error("Unexpected error in gateway: ", ex);
            message = "Internal gateway error";
        }

        errorResponse.put("timestamp", LocalDateTime.now().toString());
        //errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        //errorResponse.put("requestId", exchange.getRequest().getId());

        // Set response status
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Write response
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer dataBuffer;
        try {
            dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
        } catch (JsonProcessingException e) {
            log.error("Error writing response", e);
            dataBuffer = bufferFactory.wrap("{\"error\":\"Error processing response\"}".getBytes());
        }

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private String extractServiceNameAndFormat(String errorMessage) {
        if (errorMessage != null && errorMessage.contains("Unable to find instance for")) {
            // Extract service name from error message
            String serviceName = errorMessage.substring(errorMessage.lastIndexOf("for ") + 4);
            serviceName = serviceName.replace("\"", "").trim();
            return String.format("%s is currently unavailable. Please try again later.", serviceName);
        }
        return "The requested service is currently unavailable. Please try again later.";
    }
}