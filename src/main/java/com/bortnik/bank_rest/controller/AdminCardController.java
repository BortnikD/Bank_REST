package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.controller.validator.CardValidator;
import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.service.CardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Cards", description = "Card management endpoints for administrators")
public class AdminCardController {

    private final CardService cardService;

    @GetMapping("/users/{userId}/cards")
    public Page<CardDTO> getUserCards(
            @PathVariable UUID userId,
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return cardService.getAllUserCards(userId, pageable);
    }

    @GetMapping("/{cardId}")
    public CardDTO getUserCardById(
            @PathVariable UUID cardId
    ) {
        return cardService.getCardByIdForAdmin(cardId);
    }

    @GetMapping()
    public Page<CardDTO> getAllCards(
            @PageableDefault(
                    size = 20,
                    page = 0,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return cardService.getAllCards(pageable);
    }

    @PostMapping("/{cardId}/block")
    public CardDTO blockUserCard(
            @PathVariable UUID cardId
    ) {
        return cardService.blockCardByAdmin(cardId);
    }

    @PostMapping("/{cardId}/activate")
    public CardDTO activateUserCard(
            @PathVariable UUID cardId
    ) {
        return cardService.activateCardByAdmin(cardId);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteUserCard(
            @PathVariable UUID cardId
    ) {
        cardService.deleteCardByAdmin(cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<CardDTO> createUserCard(@RequestParam UUID userId) {
        final CardDTO card = cardService.createCardForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PostMapping("/{cardId}/top-up")
    public CardDTO topUpCardBalance(
            @PathVariable UUID cardId,
            @RequestParam BigDecimal amount
    ) {
        CardValidator.validateAmountPositive(amount);
        return cardService.topUpCardBalance(cardId, amount);
    }
}
