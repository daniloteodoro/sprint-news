package com.sprintnews.domain.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserStoriesAreInvalid extends CannotGenerateReport {
    public UserStoriesAreInvalid(String message) {
        super(message);
    }
    public UserStoriesAreInvalid(String message, Throwable cause) {
        super(message, cause);
    }
}
