package com.sprintnews.domain.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidUserInput extends CannotGenerateReport {
    public InvalidUserInput(String message) {
        super(message);
    }
    public InvalidUserInput(String message, Throwable cause) {
        super(message, cause);
    }
}
