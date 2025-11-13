package com.bortnik.bank_rest.dto.card;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CardDepositDTO {
    private UUID userId;
    private String cardNumber;
    private BigDecimal amount;
}
