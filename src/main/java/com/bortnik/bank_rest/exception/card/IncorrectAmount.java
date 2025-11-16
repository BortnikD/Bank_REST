package com.bortnik.bank_rest.exception.card;

public class IncorrectAmount extends RuntimeException {
    public IncorrectAmount(String message) {
        super(message);
    }
}
