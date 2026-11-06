package com.smartbuilding.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test-endpoint");
    }

    @Test
    @DisplayName("handleServiceException returns correct status and message")
    void handleServiceException_returnsStatusAndMessage() {
        ServiceException ex = new ServiceException("Database unavailable", HttpStatus.SERVICE_UNAVAILABLE);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServiceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(503);
        assertThat(response.getBody().message()).isEqualTo("Database unavailable");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/test-endpoint");
    }

    @Test
    @DisplayName("handleGenericException returns 500 Internal Server Error")
    void handleGenericException_returns500() {
        RuntimeException ex = new RuntimeException("Unexpected memory fault");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).contains("Unexpected memory fault");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/test-endpoint");
    }
}
