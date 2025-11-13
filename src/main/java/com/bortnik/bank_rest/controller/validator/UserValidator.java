package com.bortnik.bank_rest.controller.validator;

import com.bortnik.bank_rest.exception.BadRequest;

public class UserValidator {
    public static void validateUsername(final String username) {
        if (username == null || username.isEmpty()) {
            throw new BadRequest("Username is required");
        }
        if (username.length() < 3 || username.length() > 64) {
            throw new BadRequest("Username length should be between 3 and 64 characters");
        }
    }

    public static void validatePassword(final String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequest("Password is required");
        }
        if (password.length() < 8 || password.length() > 256) {
            throw new BadRequest("Password length should be between 8 and 256 characters");
        }
    }
}
