package com.bortnik.bank_rest.exception;

public class BadRequest extends RuntimeException {
  public BadRequest(String message) {
    super(message);
  }
}
