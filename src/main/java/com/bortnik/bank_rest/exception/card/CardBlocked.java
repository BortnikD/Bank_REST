package com.bortnik.bank_rest.exception.card;

public class CardBlocked extends RuntimeException {
  public CardBlocked(String message) {
    super(message);
  }
}
