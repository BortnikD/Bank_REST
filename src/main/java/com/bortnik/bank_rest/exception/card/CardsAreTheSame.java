package com.bortnik.bank_rest.exception.card;

public class CardsAreTheSame extends RuntimeException {
  public CardsAreTheSame(String message) {
    super(message);
  }
}
