package com.bortnik.bank_rest.controller.validator;

import com.bortnik.bank_rest.exception.BadRequest;

import java.math.BigDecimal;

public class CardValidator {
    public static void validateAmountPositive(final BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequest("Amount must be positive");
        }
    }
}
