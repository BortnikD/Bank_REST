package com.bortnik.bank_rest.exception.card;

public class CardNotFound extends RuntimeException {
    public CardNotFound(String message) {
        super(message);
    }
}
