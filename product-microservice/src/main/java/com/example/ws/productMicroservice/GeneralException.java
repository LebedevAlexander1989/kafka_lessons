package com.example.ws.productMicroservice;

import java.util.Date;

public class GeneralException {

    private final Date timestamp;
    private final String message;

    public GeneralException(Date timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }
}
