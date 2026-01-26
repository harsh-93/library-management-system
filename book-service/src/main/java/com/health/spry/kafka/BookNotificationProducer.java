package com.health.spry.kafka;


import java.util.concurrent.CompletableFuture;


import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookNotificationProducer {

    private final KafkaTemplate<String, BookNotificationEvent> kafkaTemplate;

    @Value("${kafka.topic.book-notification}")
    private String topic;

    public void sendNotification(BookNotificationEvent event) {
        log.info("Sending notification event to Kafka topic {}: {}", topic, event);
        
        CompletableFuture<SendResult<String, BookNotificationEvent>> future = 
            kafkaTemplate.send(topic, event.getBookId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent notification event: {} with offset: {} to partition: {}", 
                    event, result.getRecordMetadata().offset(),result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send notification event: {}", event, ex);
                throw new KafkaException(ex.getMessage());
            }
        });
    }
}
