package com.bortnik.bank_rest.controller;

import com.bortnik.bank_rest.controller.validator.CardValidator;
import com.bortnik.bank_rest.dto.ApiError;
import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.service.card.AdminCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Admin — Cards", description = "Card management endpoints for administrators")
public class AdminCardController {

    private final AdminCardService adminCardService;


    @Operation(summary = "Get user's cards", description = "Returns a paginated list of cards belonging to the specified user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cards successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/users/{userId}/cards")
    public Page<CardDTO> getUserCards(
            @Parameter(description = "User ID")
            @PathVariable
            UUID userId,
            @RequestParam(required = false)
            CardStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        if (status != null) {
            return adminCardService.getCardsByUserIdAndStatus(userId, status, pageable);
        }
        return adminCardService.getAllUserCards(userId, pageable);
    }


    @Operation(summary = "Get card by ID", description = "Returns a card by its ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Card found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{cardId}")
    public CardDTO getUserCardById(
            @Parameter(description = "Card ID") @PathVariable UUID cardId
    ) {
        return adminCardService.getCardById(cardId);
    }


    @Operation(summary = "Get all cards", description = "Returns a paginated list of all cards in the system")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cards successfully retrieved"
            )
    })
    @GetMapping
    public Page<CardDTO> getAllCards(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false)
            CardStatus status
    ) {
        if (status != null) {
            return adminCardService.getAllCardsByStatus(status, pageable);
        }
        return adminCardService.getAllCards(pageable);
    }


    @Operation(summary = "Block user card", description = "Blocks a specific user card")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Card successfully blocked"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{cardId}/block")
    public CardDTO blockUserCard(
            @Parameter(description = "Card ID to block") @PathVariable UUID cardId
    ) {
        return adminCardService.blockCard(cardId);
    }


    @Operation(summary = "Activate user card", description = "Activates a specific user card")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Card successfully activated"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{cardId}/activate")
    public CardDTO activateUserCard(
            @Parameter(description = "Card ID to activate") @PathVariable UUID cardId
    ) {
        return adminCardService.activateCard(cardId);
    }


    @Operation(summary = "Delete user card", description = "Deletes a specific card permanently")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Card deleted"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Card not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteUserCard(
            @Parameter(description = "Card ID to delete") @PathVariable UUID cardId
    ) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Create card for user", description = "Creates a new card for a specific user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Card created successfully",
                    content = @Content(schema = @Schema(implementation = CardDTO.class))
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<CardDTO> createUserCard(
            @Parameter(description = "User ID to associate with the new card") @RequestParam UUID userId
    ) {
        final CardDTO card = adminCardService.createCardForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }


    @Operation(summary = "Top-up card balance", description = "Adds money to a user's card balance")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid top-up amount",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Card not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{cardId}/top-up")
    public CardDTO topUpCardBalance(
            @Parameter(description = "Card ID") @PathVariable UUID cardId,
            @Parameter(description = "Amount to top up") @RequestParam BigDecimal amount
    ) {
        CardValidator.validateAmountPositive(amount);
        return adminCardService.topUpCardBalance(cardId, amount);
    }
}
