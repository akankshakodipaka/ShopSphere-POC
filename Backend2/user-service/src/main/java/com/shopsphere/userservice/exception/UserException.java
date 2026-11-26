package com.shopsphere.userservice.exception;

import org.springframework.http.HttpStatus;

/** A business error that carries the HTTP status to return. */
public class UserException extends RuntimeException {

    private final HttpStatus status;

    public UserException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
