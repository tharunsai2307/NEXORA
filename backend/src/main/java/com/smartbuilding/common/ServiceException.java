package com.smartbuilding.common;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
    private final HttpStatus status;

    public ServiceException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
