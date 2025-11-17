package com.bortnik.bank_rest.dto.card;

import com.bortnik.bank_rest.entity.CardStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class CardDTO {
    UUID id;
    UUID userId;
    String cardNumber;
    LocalDate expirationDate;
    CardStatus status;
    BigDecimal balance;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
