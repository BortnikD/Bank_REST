package com.bortnik.bank_rest.exception.card;

public class CardExpired extends RuntimeException {
  public CardExpired(String message) {
    super(message);
  }
}
