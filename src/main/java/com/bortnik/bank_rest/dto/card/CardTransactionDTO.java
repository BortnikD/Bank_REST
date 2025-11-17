package com.bortnik.bank_rest.dto.card;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionDTO {
    private UUID fromCardId;
    private UUID toCardId;
    private BigDecimal amount;
}
