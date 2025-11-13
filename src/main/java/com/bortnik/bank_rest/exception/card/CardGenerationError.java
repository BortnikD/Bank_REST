package com.bortnik.bank_rest.exception.card;

public class CardGenerationError extends RuntimeException {
    public CardGenerationError(String message) {
        super(message);
    }
}
