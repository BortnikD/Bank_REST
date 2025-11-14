package com.bortnik.bank_rest.dto.card;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CardTransactionDTO {
    final UUID fromCardId;
    final UUID toCardId;
    final BigDecimal amount;
}
