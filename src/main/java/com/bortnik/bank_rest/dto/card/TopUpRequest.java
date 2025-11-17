package com.bortnik.bank_rest.dto.card;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {
    private BigDecimal amount;
}
