package com.health.spry.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/book-service", method = {
            RequestMethod.GET, 
            RequestMethod.POST, 
            RequestMethod.PUT, 
            RequestMethod.DELETE, 
            RequestMethod.PATCH
    })
    public ResponseEntity<Map<String, Object>> bookServiceFallback() {
        return createFallbackResponse("BOOK-SERVICE");
    }

    @RequestMapping(value = "/login-service", method = {
            RequestMethod.GET, 
            RequestMethod.POST, 
            RequestMethod.PUT, 
            RequestMethod.DELETE, 
            RequestMethod.PATCH
    })
    public ResponseEntity<Map<String, Object>> loginServiceFallback() {
        return createFallbackResponse("LOGIN-SERVICE");
    }
    
    @RequestMapping(value = "/default/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        return createFallbackResponse("Requested service");
    }
    
    @RequestMapping(value = "/signup-service", method = {
            RequestMethod.GET, 
            RequestMethod.POST, 
            RequestMethod.PUT, 
            RequestMethod.DELETE, 
            RequestMethod.PATCH
    })
    public ResponseEntity<Map<String, Object>> signupServiceFallback() {
        return createFallbackResponse("SIGNUP-SERVICE");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        //response.put("code", "SERVICE_001");
        response.put("message", String.format("%s is currently unavailable. Please try again later.", serviceName));
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}