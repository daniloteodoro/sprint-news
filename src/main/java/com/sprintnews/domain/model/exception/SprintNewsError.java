package com.sprintnews.domain.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SprintNewsError extends RuntimeException {
    public SprintNewsError(String message) {
        super(message);
    }
    public SprintNewsError(String message, Throwable cause) {
        super(message, cause);
    }
}
