package com.digisecurebank.payment_service.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictingResourcesException extends RuntimeException {
    public ConflictingResourcesException(String message) {
        super(message);
    }
}
