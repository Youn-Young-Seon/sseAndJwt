package com.example.test.jwt;

import org.springframework.security.core.AuthenticationException;

public class InvalidBearerToken extends AuthenticationException {
    public InvalidBearerToken(String message) {
        super(message);
    }
}
