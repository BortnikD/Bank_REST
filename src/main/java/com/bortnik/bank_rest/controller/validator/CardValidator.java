package com.bortnik.bank_rest.controller.validator;

import com.bortnik.bank_rest.exception.BadRequest;

import java.math.BigDecimal;
import java.util.UUID;

public class CardValidator {
    public static void validateAmountPositive(final BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequest("Amount must be positive");
        }
    }

    public static void validateDifferentCards(final UUID fromCardId, final UUID toCardId) {
        if (fromCardId.equals(toCardId)) {
            throw new BadRequest("From and To card IDs must be different");
        }
    }
}
