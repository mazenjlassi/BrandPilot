package com.example.metatry.Exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_returnsBadRequest() {
        ResponseEntity<String> response = handler.handleRuntimeException(
                new RuntimeException("Something went wrong")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Something went wrong");
    }

    @Test
    void handleRuntimeException_returnsErrorMessage() {
        ResponseEntity<String> response = handler.handleRuntimeException(
                new RuntimeException("Post not found")
        );

        assertThat(response.getBody()).isEqualTo("Post not found");
    }
}
