package com.bortnik.bank_rest.dto.card;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CardCreateDTO {
    private UUID userId;
}
