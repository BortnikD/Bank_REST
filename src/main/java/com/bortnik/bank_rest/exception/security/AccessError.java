package com.bortnik.bank_rest.exception.security;

public class AccessError extends RuntimeException {
    public AccessError(String message) {
        super(message);
    }
}
