package com.health.spry.kafka;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import com.health.spry.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookNotificationConsumer {

    private final NotificationService notificationService;

    /**
     * Reliable Kafka consumer with:
     * 1. Automatic retries with exponential backoff
     * 2. Dead Letter Queue (DLQ) for failed messages after all retries
     * 
     * Retry Strategy:
     * - Attempt 1: Immediate
     * - Attempt 2: After 2 seconds
     * - Attempt 3: After 4 seconds
     * - Attempt 4: After 8 seconds
     * - After 4 attempts: Send to DLQ
     * 
     * NOTE: When using @RetryableTopic, Spring handles acknowledgment automatically.
     * Manual ACK is not compatible with @RetryableTopic.
     */
    @RetryableTopic(
            attempts = "4",  // Total attempts (1 original + 3 retries)
            backoff = @Backoff(
                    delay = 2000,      // Initial delay: 2 seconds
                    multiplier = 2.0,  // Exponential backoff multiplier
                    maxDelay = 10000   // Maximum delay: 10 seconds
            ),
            autoCreateTopics = "true",  // Auto-create retry and DLT topics
            include = {Exception.class},  // Retry on any exception
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,  // Naming: topic-retry-0, topic-retry-1, etc.
            dltTopicSuffix = "-dlt"  // DLT topic suffix
    )
    @KafkaListener(
            topics = "${kafka.topic.book-notification}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeNotification(
            @Payload BookNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("┌─────────────────────────────────────────────────────────────");
        log.info("│ Consuming message from Kafka");
        log.info("│ Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        log.info("│ Event: {}", event);
        log.info("└─────────────────────────────────────────────────────────────");
        
        // Process the notification
        // If this throws an exception, @RetryableTopic will automatically retry
        notificationService.processNotification(event);
        
        log.info("✅ Successfully processed notification for user: {} (offset: {})", 
                event.getUserId(), offset);
        
        // NOTE: No manual acknowledgment needed - Spring handles it automatically
        // after successful processing or after sending to DLT
    }

    /**
     * Dead Letter Topic (DLT) Handler
     * 
     * This method is called when a message fails after all retry attempts.
     * Messages that reach here have failed 4 times and are sent to the DLT.
     * 
     * Actions:
     * 1. Log the failure with full details
     * 2. Store in database for manual review (future enhancement)
     * 3. Send alert to monitoring system (future enhancement)
     */
    @DltHandler
    public void handleDlt(
            @Payload BookNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(value = KafkaHeaders.EXCEPTION_STACKTRACE, required = false) String stackTrace) {
        
        log.error("╔═════════════════════════════════════════════════════════════");
        log.error("║ MESSAGE SENT TO DEAD LETTER QUEUE");
        log.error("║ All retry attempts exhausted!");
        log.error("╠═════════════════════════════════════════════════════════════");
        log.error("║ DLT Topic: {}", topic);
        log.error("║ Original Topic: {}", topic.replace("-dlt", ""));
        log.error("║ Partition: {}, Offset: {}", partition, offset);
        log.error("║ Event: {}", event);
        log.error("║ Final Error: {}", exceptionMessage);
        log.error("╠═════════════════════════════════════════════════════════════");
        log.error("║ Stack Trace:");
        if (stackTrace != null) {
            log.error("{}", stackTrace);
        }
        log.error("╠═════════════════════════════════════════════════════════════");
        log.error("║ ACTION REQUIRED:");
        log.error("║ 1. Review the error message above");
        log.error("║ 2. Fix the root cause");
        log.error("║ 3. Manually replay this message if needed");
        log.error("╚═════════════════════════════════════════════════════════════");
        
        // TODO: Store in database for manual review
        // deadLetterRepository.save(new DeadLetterMessage(event, exceptionMessage, topic, offset));
        
        // TODO: Send alert to monitoring system
        // alertService.sendAlert("DLT Message", event, exceptionMessage);
        
        log.warn("⚠️ DLT message logged. Manual intervention may be required.");
        
        // NOTE: No manual acknowledgment needed - Spring handles it automatically
    }
}