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
	
	private static String fallbackSuffix="service is not available to gateway at the moment. "
			+ "Either {} has not finished registering to the gateway ribbon loadbalancer or it is not running at all.If you know that the USER-SIGNUP-SERVICE service is up and running then please wait for few seconds Until you see a console log ' Flipping property: USER-SIGNUP-SERVICE.ribbon.ActiveConnectionsLimit.....' .This log will confirm that the service is available for gaateway routing. FYI: Once you are able to access resource, this will not happen again, unless the USER-SIGNUP-SERVICE service is down in which case you have to contact the creator!";

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
        response.put("message", String.format("%s is currently unavailable to the gateway. Please try again later. It may take few seconds to register service . "
        		+ "If it does not work in few soconds, please check with Support team", serviceName));
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}