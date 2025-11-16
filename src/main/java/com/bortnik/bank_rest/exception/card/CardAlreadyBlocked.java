package com.bortnik.bank_rest.exception.card;

public class CardAlreadyBlocked extends RuntimeException {
    public CardAlreadyBlocked(String message) {
        super(message);
    }
}
