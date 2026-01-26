package com.health.spry.service;

import org.springframework.stereotype.Service;

import com.health.spry.kafka.BookNotificationEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    public void processNotification(BookNotificationEvent event) {
        log.info("==============================================");
        log.info("NOTIFICATION PREPARED");
        log.info("==============================================");
        log.info("Notification prepared for user_id: {}", event.getUserId());
        log.info("Book [{}] is now available", event.getBookTitle());
        log.info("Event Type: {}", event.getEventType());
        log.info("Message: {}", event.getMessage());
        log.info("==============================================");

        // In a real application, this would:
        // 1. Send an email to the user
        // 2. Send a push notification to mobile devices
        // 3. Create an in-app notification
        // 4. Store notification in database for later retrieval
        // 5. Send SMS if configured

        // Simulating some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Notification successfully sent to user: {}", event.getUserId());
        throw new RuntimeException("Simulated failure for retry");
    }
}
