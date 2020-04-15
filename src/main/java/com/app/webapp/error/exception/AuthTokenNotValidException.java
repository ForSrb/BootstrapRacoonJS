package com.app.webapp.error.exception;

import com.app.webapp.error.ErrorDomains;
import lombok.Getter;

@Getter
public class AuthTokenNotValidException extends RuntimeException {
    private final String domain = ErrorDomains.AUTH;

    public AuthTokenNotValidException(String message) {
        super(message);
    }
}
