package com.bortnik.bank_rest.util.mappers;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.Card;

public class CardMapper {
    public static CardDTO toCardDTO(Card card) {
        return CardDTO.builder()
                .id(card.getId())
                .userId(card.getUserId())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .createdAt(card.getCreatedAt())
                .build();
    }
}
