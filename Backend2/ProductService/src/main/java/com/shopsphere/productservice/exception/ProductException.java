package com.shopsphere.productservice.exception;
import org.springframework.http.HttpStatus;

/** A business error that carries the HTTP status to return. */
public class ProductException extends RuntimeException {

    private final HttpStatus status;

    public ProductException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
