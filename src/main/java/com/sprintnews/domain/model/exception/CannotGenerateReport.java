package com.sprintnews.domain.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CannotGenerateReport extends SprintNewsError {
    public CannotGenerateReport(String message) {
        super(message);
    }
    public CannotGenerateReport(String message, Throwable cause) {
        super(message, cause);
    }
}
