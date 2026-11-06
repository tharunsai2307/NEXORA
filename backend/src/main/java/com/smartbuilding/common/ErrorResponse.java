package com.smartbuilding.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors,
        Instant timestamp
) {
    public record ValidationError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, null, Instant.now());
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        return new ErrorResponse(status, error, message, path, validationErrors, Instant.now());
    }
}
