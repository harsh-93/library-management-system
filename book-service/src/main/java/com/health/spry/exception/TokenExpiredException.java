package com.health.spry.exception;

public class TokenExpiredException extends RuntimeException {
    private final String errorCode;
    
    public TokenExpiredException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}