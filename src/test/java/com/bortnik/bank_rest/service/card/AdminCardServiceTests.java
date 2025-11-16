package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.*;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import com.bortnik.bank_rest.repository.CardRepository;
import com.bortnik.bank_rest.security.card_encryption.CardEncryptionService;
import com.bortnik.bank_rest.service.UserService;
import com.bortnik.bank_rest.util.SimpleCardNumberGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminCardServiceTests {

    private final CardRepository cardRepository = mock(CardRepository.class);
    private final UserService userService = mock(UserService.class);
    private final CoreCardService coreCardService = mock(CoreCardService.class);
    private final CardEncryptionService cardEncryptionService = mock(CardEncryptionService.class);

    private final AdminCardService adminCardService = new AdminCardService(
            cardRepository,
            userService,
            coreCardService,
            cardEncryptionService
    );

    @Test
    public void blockCard_success() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        adminCardService.blockCard(card.getId());

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertNotNull(card.getUpdatedAt());
    }

    @Test
    public void blockCard_shouldThrowCardNotFound() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        var exception = assertThrows(CardNotFound.class, () ->
                adminCardService.blockCard(cardId));

        assertEquals("Card with id " + cardId + " not found", exception.getMessage());
    }

    @Test
    public void blockCard_shouldThrowCardExpired() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.EXPIRED)
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        var exception = assertThrows(CardExpired.class, () ->
                adminCardService.blockCard(card.getId()));

        assertEquals("card is expired", exception.getMessage());
    }

    @Test
    public void blockCard_shouldThrowCardAlreadyBlocked() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        var exception = assertThrows(CardAlreadyBlocked.class, () ->
                adminCardService.blockCard(card.getId()));

        assertEquals("Card is already blocked", exception.getMessage());
    }

    @Test
    public void activateCard_success() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        adminCardService.activateCard(card.getId());

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertNotNull(card.getUpdatedAt());
    }

    @Test
    public void activateCard_shouldThrowCardNotFound() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        var exception = assertThrows(CardNotFound.class, () ->
                adminCardService.activateCard(cardId));

        assertEquals("Card with id " + cardId + " not found", exception.getMessage());
    }

    @Test
    public void activateCard_shouldThrowCardExpired() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.EXPIRED)
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        var exception = assertThrows(CardExpired.class, () ->
                adminCardService.activateCard(card.getId()));

        assertEquals("card is expired", exception.getMessage());
    }

    @Test
    public void activateCard_shouldThrowCardAlreadyActivated() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        var exception = assertThrows(CardAlreadyActivated.class, () ->
                adminCardService.activateCard(card.getId()));

        assertEquals("Card is already activated", exception.getMessage());
    }


    @Test
    public void toUpCardBalance_success() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(100))
                .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        adminCardService.topUpCardBalance(card.getId(), BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), card.getBalance());
    }

    @Test
    public void topUpCardBalance_shouldThrowCardNotFound() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        var exception = assertThrows(CardNotFound.class, () ->
                adminCardService.topUpCardBalance(cardId, BigDecimal.valueOf(50)));

        assertEquals("Card with id " + cardId + " not found", exception.getMessage());
    }

    @Test
    public void toUpCardBalance_shouldThrowIncorrectAmount() {
        UUID cardId = UUID.randomUUID();
        BigDecimal incorrectAmount = BigDecimal.valueOf(-100);

        var exception = assertThrows(IncorrectAmount.class, () ->
                adminCardService.topUpCardBalance(cardId, incorrectAmount));

        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void createCardForUser_shouldThrowUserNotFound_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userService.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFound.class,
                () -> adminCardService.createCardForUser(userId));

        verify(cardRepository, never()).save(any());
        verify(cardEncryptionService, never()).encrypt(any());
    }

    @Test
    void createCardForUser_shouldCreateCardCorrectly() {
        final int EXPIRATION_YEARS = 5;
        UUID userId = UUID.randomUUID();

        when(userService.existsById(userId)).thenReturn(true);

        String rawCardNumber = "1234567812345678";
        String encrypted = "encrypted-number";

        MockedStatic<SimpleCardNumberGenerator> mockedGenerator =
                mockStatic(SimpleCardNumberGenerator.class);
        mockedGenerator.when(SimpleCardNumberGenerator::generate).thenReturn(rawCardNumber);

        when(cardEncryptionService.encrypt(rawCardNumber)).thenReturn(encrypted);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        Card savedCard = Card.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .cardNumber(encrypted)
                .lastFourDigits("5678")
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(EXPIRATION_YEARS))
                .build();

        when(cardRepository.save(any())).thenReturn(savedCard);

        CardDTO result = adminCardService.createCardForUser(userId);

        mockedGenerator.close();

        verify(cardRepository).save(cardCaptor.capture());
        Card captured = cardCaptor.getValue();

        assertEquals(userId, captured.getUserId());
        assertEquals(encrypted, captured.getCardNumber());
        assertEquals("5678", captured.getLastFourDigits());
        assertEquals(BigDecimal.ZERO, captured.getBalance());
        assertEquals(CardStatus.ACTIVE, captured.getStatus());
        assertEquals(LocalDate.now().plusYears(EXPIRATION_YEARS), captured.getExpirationDate());

        assertEquals(savedCard.getId(), result.getId());
        assertEquals("5678",
                result.getCardNumber().substring(result.getCardNumber().length() - 4));
        assertEquals(savedCard.getStatus(), result.getStatus());
        assertEquals(savedCard.getExpirationDate(), result.getExpirationDate());
    }
}
