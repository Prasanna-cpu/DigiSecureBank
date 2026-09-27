package com.digisecurebank.auth_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictingResourcesException extends RuntimeException {
    public ConflictingResourcesException(String message) {
        super(message);
    }
}
