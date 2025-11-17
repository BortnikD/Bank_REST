package com.bortnik.bank_rest.controller.user;

import com.bortnik.bank_rest.controller.validator.CardValidator;
import com.bortnik.bank_rest.dto.ApiResponse;
import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.dto.card.CardTransactionDTO;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.security.services.UserDetailsImpl;
import com.bortnik.bank_rest.service.card.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cards", description = "Endpoints for managing user cards and transactions")
public class CardController {

    private final UserCardService userCardService;

    @Operation(
            summary = "Get all cards of the authenticated user",
            description = "Returns a paginated list of cards owned by the authenticated user."
    )
    @GetMapping("/my")
    public ApiResponse<Page<CardDTO>> getAllUserCards(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            UserDetailsImpl userDetailsImpl,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false)
            CardStatus status
    ) {
        if (status != null) {
            return ApiResponse.<Page<CardDTO>>builder()
                    .responseData(userCardService.getCardsByUserIdAndStatus(userDetailsImpl.getId(), status, pageable))
                    .build();
        }
        return ApiResponse.<Page<CardDTO>>builder()
                .responseData(userCardService.getAllUserCards(userDetailsImpl.getId(), pageable))
                .build();
    }

    @Operation(
            summary = "Get card by ID",
            description = "Returns details of a specific card of the authenticated user."
    )
    @GetMapping("/{cardId}")
    public ApiResponse<CardDTO> getCardById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Parameter(description = "Card UUID", required = true)
            @PathVariable UUID cardId
    ) {
        return ApiResponse.<CardDTO>builder()
                .responseData(userCardService.getUserCardById(userDetailsImpl.getId(), cardId))
                .build();
    }

    @Operation(
            summary = "Internal transfer between user's cards",
            description = "Transfers money between two cards of the authenticated user."
    )
    @PostMapping("/transfer")
    public ResponseEntity<Void> internalTransfer(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Parameter(description = "Transfer details", required = true)
            @RequestBody CardTransactionDTO transactionDTO
    ) {
        CardValidator.validateAmountPositive(transactionDTO.getAmount());
        CardValidator.validateDifferentCards(transactionDTO.getFromCardId(), transactionDTO.getToCardId());
        userCardService.internalTransfer(transactionDTO, userDetailsImpl.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Block a card",
            description = "Blocks a specific card of the authenticated user."
    )
    @PostMapping("/{cardId}/block")
    public ApiResponse<CardDTO> blockCard(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Parameter(description = "Card UUID", required = true)
            @PathVariable UUID cardId
    ) {
        return ApiResponse.<CardDTO>builder()
                .responseData(userCardService.blockCard(userDetailsImpl.getId(), cardId))
                .build();
    }
}
