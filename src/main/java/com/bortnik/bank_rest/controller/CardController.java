package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.controller.validator.CardValidator;
import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.dto.card.CardTransactionDTO;
import com.bortnik.bank_rest.security.services.UserDetailsImpl;
import com.bortnik.bank_rest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/my")
    public Page<CardDTO> getAllUserCards(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return cardService.getAllUserCards(userDetailsImpl.getId(), pageable);
    }

    @GetMapping("/{cardId}")
    public CardDTO getCardById(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @PathVariable UUID cardId
    ) {
        return cardService.getUserCardById(userDetailsImpl.getId(), cardId);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> internalTransfer(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @RequestBody CardTransactionDTO transactionDTO
    ) {
        CardValidator.validateAmountPositive(transactionDTO.getAmount());
        cardService.internalTransfer(transactionDTO, userDetailsImpl.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/block")
    public CardDTO blockCard(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @PathVariable UUID cardId
    ) {
        return cardService.blockCard(userDetailsImpl.getId(), cardId);
    }
}
