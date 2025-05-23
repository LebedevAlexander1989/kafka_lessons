package com.example.ws.emailNotificationMicroservice.exception;

public class RetryableException extends RuntimeException {


    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(Throwable cause) {
        super(cause);
    }
}
