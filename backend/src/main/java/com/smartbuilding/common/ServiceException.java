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

    public static ServiceException badRequest(String message) {
        return new ServiceException(message, HttpStatus.BAD_REQUEST);
    }

    public static ServiceException notFound(String message) {
        return new ServiceException(message, HttpStatus.NOT_FOUND);
    }

    public static ServiceException unauthorized(String message) {
        return new ServiceException(message, HttpStatus.UNAUTHORIZED);
    }

    public static ServiceException forbidden(String message) {
        return new ServiceException(message, HttpStatus.FORBIDDEN);
    }

    public static ServiceException conflict(String message) {
        return new ServiceException(message, HttpStatus.CONFLICT);
    }

    public static ServiceException internal(String message) {
        return new ServiceException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
