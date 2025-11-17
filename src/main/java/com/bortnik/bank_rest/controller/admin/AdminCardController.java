package com.bortnik.bank_rest.controller.admin;

import com.bortnik.bank_rest.controller.validator.CardValidator;
import com.bortnik.bank_rest.dto.ApiResponse;
import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.dto.card.TopUpRequest;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.service.card.AdminCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin — Cards", description = "Card management endpoints for administrators")
public class AdminCardController {

    private final AdminCardService adminCardService;

    @Operation(summary = "Get user's cards", description = "Returns a paginated list of cards belonging to the specified user")
    @GetMapping("/users/{userId}/cards")
    public ApiResponse<Page<CardDTO>> getUserCards(
            @Parameter(description = "User ID")
            @PathVariable
            UUID userId,
            @RequestParam(required = false)
            CardStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        if (status != null) {
            return ApiResponse.<Page<CardDTO>>builder()
                    .responseData(adminCardService.getCardsByUserIdAndStatus(userId, status, pageable))
                    .build();
        }
        return ApiResponse.<Page<CardDTO>>builder()
                .responseData(adminCardService.getAllUserCards(userId, pageable))
                .build();
    }

    @Operation(summary = "Get card by ID", description = "Returns a card by its ID")
    @GetMapping("/{cardId}")
    public ApiResponse<CardDTO> getUserCardById(
            @Parameter(description = "Card ID") @PathVariable UUID cardId
    ) {
        return ApiResponse.<CardDTO>builder()
                .responseData(adminCardService.getCardById(cardId))
                .build();
    }

    @Operation(summary = "Get all cards", description = "Returns a paginated list of all cards in the system")
    @GetMapping
    public ApiResponse<Page<CardDTO>> getAllCards(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false)
            CardStatus status
    ) {
        if (status != null) {
            return ApiResponse.<Page<CardDTO>>builder()
                    .responseData(adminCardService.getAllCardsByStatus(status, pageable))
                    .build();
        }
        return ApiResponse.<Page<CardDTO>>builder()
                .responseData(adminCardService.getAllCards(pageable))
                .build();
    }

    @Operation(summary = "Block user card", description = "Blocks a specific user card")
    @PostMapping("/{cardId}/block")
    public ApiResponse<CardDTO> blockUserCard(
            @Parameter(description = "Card ID to block") @PathVariable UUID cardId
    ) {
        return ApiResponse.<CardDTO>builder()
                .responseData(adminCardService.blockCard(cardId))
                .build();
    }

    @Operation(summary = "Activate user card", description = "Activates a specific user card")
    @PostMapping("/{cardId}/activate")
    public ApiResponse<CardDTO> activateUserCard(
            @Parameter(description = "Card ID to activate") @PathVariable UUID cardId
    ) {
        return ApiResponse.<CardDTO>builder()
                .responseData(adminCardService.activateCard(cardId))
                .build();
    }

    @Operation(summary = "Delete user card", description = "Deletes a specific card permanently")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteUserCard(
            @Parameter(description = "Card ID to delete") @PathVariable UUID cardId
    ) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create card for user", description = "Creates a new card for a specific user")
    @PostMapping
    public ResponseEntity<ApiResponse<CardDTO>> createUserCard(
            @Parameter(description = "User ID to associate with the new card") @RequestParam UUID userId
    ) {
        final CardDTO card = adminCardService.createCardForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CardDTO>builder()
                        .responseData(card)
                        .build()
        );
    }

    @Operation(summary = "Top-up card balance", description = "Adds money to a user's card balance")
    @PostMapping("/{cardId}/top-up")
    public ApiResponse<CardDTO> topUpCardBalance(
            @Parameter(description = "Card ID") @PathVariable UUID cardId,
            @Parameter(description = "Amount to top up") @RequestBody TopUpRequest topUpRequest
    ) {
        CardValidator.validateAmountPositive(topUpRequest.getAmount());
        return ApiResponse.<CardDTO>builder()
                .responseData(adminCardService.topUpCardBalance(cardId, topUpRequest.getAmount()))
                .build();
    }
}
