package com.health.spry.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookNotificationEvent {
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String eventType;
    private String message;
}
