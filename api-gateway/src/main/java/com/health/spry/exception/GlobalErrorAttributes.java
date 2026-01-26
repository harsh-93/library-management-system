package com.health.spry.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        if (error instanceof NotFoundException) {
            log.error("Service not found: {}", error.getMessage());
            
            errorAttributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            errorAttributes.put("error", "Service Unavailable");
            errorAttributes.put("code", "SERVICE_001");
            errorAttributes.put("message", "The requested service is currently unavailable. Please try again later.");
            errorAttributes.put("timestamp", LocalDateTime.now());
            errorAttributes.remove("trace"); // Remove stack trace from response
        }

        return errorAttributes;
    }
}