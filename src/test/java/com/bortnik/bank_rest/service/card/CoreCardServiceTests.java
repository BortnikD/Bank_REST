package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.CardBlocked;
import com.bortnik.bank_rest.exception.card.CardExpired;
import com.bortnik.bank_rest.repository.CardRepository;
import com.bortnik.bank_rest.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoreCardServiceTests {

    private final CardRepository cardRepository = mock(CardRepository.class);
    private final UserService userService = mock(UserService.class);
    private final CoreCardService coreCardService = new CoreCardService(cardRepository, userService);

    @Test
    void validateActiveCard_success() {
        Card card = Card.builder()
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(5))
                .build();

        coreCardService.validateActiveCard(card);
    }

    @Test
    void validateActiveCard_throwsCardBlocked() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.BLOCKED)
                .expirationDate(LocalDate.now().plusYears(5))
                .build();

        var exception = assertThrows(CardBlocked.class, () ->
                coreCardService.validateActiveCard(card));

        assertEquals("Card with ID " + card.getId() + " is blocked", exception.getMessage());
    }

    @Test
    void validateActiveCard_throwsCardExpired_WhenCardStatusIsExpired() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.EXPIRED)
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        var exception = assertThrows(CardExpired.class, () ->
                coreCardService.validateActiveCard(card));

        assertEquals("Card with ID " + card.getId() + " is expired", exception.getMessage());
    }


    @Test
    void validateActiveCard_throwsCardExpired_WhenCardIsPastExpirationDate() {
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .status(CardStatus.ACTIVE) // status is ACTIVE but the expiration date is past
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        when(cardRepository.save(card)).thenReturn(card);

        var exception = assertThrows(CardExpired.class, () ->
                coreCardService.validateActiveCard(card));

        assertEquals(CardStatus.EXPIRED, card.getStatus());
        assertTrue(exception.getMessage().contains("has expired on"));
    }
}
