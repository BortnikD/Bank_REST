package com.bortnik.bank_rest.dto.card;

import com.bortnik.bank_rest.entity.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CardDTO {
    private UUID id;
    private UUID userId;
    private String cardNumber;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
