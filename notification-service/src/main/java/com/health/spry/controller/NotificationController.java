package com.health.spry.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notification", description = "Notification service monitoring and health check APIs")
public class NotificationController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the notification service is running and consuming Kafka messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification Service is running and consuming Kafka messages");
    }
}
