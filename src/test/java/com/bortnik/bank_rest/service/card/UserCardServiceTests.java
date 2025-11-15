package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.dto.card.CardTransactionDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.CardBlocked;
import com.bortnik.bank_rest.exception.card.CardExpired;
import com.bortnik.bank_rest.exception.card.CardNotFound;
import com.bortnik.bank_rest.exception.card.InsufficientFunds;
import com.bortnik.bank_rest.exception.security.AccessError;
import com.bortnik.bank_rest.repository.CardRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserCardServiceTests {

    private final CardRepository cardRepository = mock(CardRepository.class);
    private final CoreCardService coreCardService = mock(CoreCardService.class);
    private final UserCardService userCardService = new UserCardService(cardRepository, coreCardService);

    @Test
    void blockCard_success() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Card card = Card.builder()
                .id(cardId)
                .userId(userId)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        CardDTO cardDTO = userCardService.blockCard(userId, cardId);

        assertEquals(CardStatus.BLOCKED, cardDTO.getStatus());
        assertNotNull(card.getUpdatedAt());
    }

    @Test
    void blockCard_shouldThrowsCardNotFound() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        var exception = assertThrows(CardNotFound.class, () ->
                userCardService.blockCard(userId, cardId));

        assertEquals("Card with number " + cardId + " not found", exception.getMessage());
    }

    @Test
    void blockCard_shouldThrowsAccessError() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Card existingCard = Card.builder()
                .id(cardId)
                .userId(UUID.randomUUID()) // different userId
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));

        var exception = assertThrows(AccessError.class, () ->
                userCardService.blockCard(userId, cardId));

        assertEquals("User with ID " + userId + " does not own card with number " + cardId, exception.getMessage());
    }

    @Test
    void internalTransfer_success() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(fromCard)).thenReturn(fromCard);
        when(cardRepository.save(toCard)).thenReturn(toCard);

        userCardService.internalTransfer(transactionDTO, userId);

        assertEquals(BigDecimal.valueOf(400), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(100), toCard.getBalance());
    }

    @Test
    void internalTransfer_shouldThrowInsufficientFunds() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(600);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        var exception = assertThrows(InsufficientFunds.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));

        assertEquals("Insufficient funds on card " + fromCardId, exception.getMessage());
    }

    @Test
    void internalTransfer_shouldThrowAccessError_WhenUserDoesntOwnTheFirstCard() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(UUID.randomUUID()) // different userId
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));

        var exception = assertThrows(AccessError.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));

        assertEquals("User with ID " + userId + " does not own card with number " + fromCardId, exception.getMessage());
    }

    @Test
    void internalTransfer_shouldThrowAccessError_WhenUserDoesntOwnTheSecondCard() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .userId(UUID.randomUUID()) // different userId
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        var exception = assertThrows(AccessError.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));

        assertEquals("User with ID " + userId + " does not own card with number " + toCardId, exception.getMessage());
    }

    @Test
    void internalTransfer_shouldThrowCardNotFound_WhenFromCardDoesntExist() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.empty());

        var exception = assertThrows(CardNotFound.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));

        assertEquals("Card with number " + fromCardId + " not found", exception.getMessage());
    }

    @Test
    void internalTransfer_shouldThrowCardNotFound_WhenToCardDoesntExist() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.empty());

        var exception = assertThrows(CardNotFound.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));

        assertEquals("Card with number " + toCardId + " not found", exception.getMessage());
    }

    @Test
    void internalTransfer_shouldThrowBlockedCard_WhenFromCardIsBlocked() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        doThrow(new CardBlocked("Card with ID " + fromCard + " is blocked"))
                .when(coreCardService).validateActiveCard(fromCard);

        assertThrows(CardBlocked.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));
    }

    @Test
    void internalTransfer_shouldThrowBlockedCard_WhenToCardIsBlocked() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        doThrow(new CardBlocked("Card with ID " + toCardId + " is blocked"))
                .when(coreCardService).validateActiveCard(toCard);

        assertThrows(CardBlocked.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));
    }

    @Test
    void internalTransfer_shouldThrowExpiredCard_WhenFromCardIsExpired() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.EXPIRED)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        doThrow(new CardExpired("Card with ID " + fromCard + " is expired"))
                .when(coreCardService).validateActiveCard(fromCard);

        assertThrows(CardExpired.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));
    }

    @Test
    void internalTransfer_shouldThrowExpiredCard_WhenToCardIsExpired() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        UUID userId = UUID.randomUUID();

        CardTransactionDTO transactionDTO = CardTransactionDTO.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .status(CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.EXPIRED)
                .build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        doThrow(new CardExpired("Card with ID " + toCardId + " is expired"))
                .when(coreCardService).validateActiveCard(toCard);

        assertThrows(CardExpired.class, () ->
                userCardService.internalTransfer(transactionDTO, userId));
    }
}
