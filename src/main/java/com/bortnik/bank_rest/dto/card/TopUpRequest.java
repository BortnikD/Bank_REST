package com.bortnik.bank_rest.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TopUpRequest {
    private BigDecimal amount;
}
