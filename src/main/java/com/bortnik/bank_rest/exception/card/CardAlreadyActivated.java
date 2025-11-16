package com.bortnik.bank_rest.exception.card;

public class CardAlreadyActivated extends RuntimeException {
    public CardAlreadyActivated(String message) {
        super(message);
    }
}
