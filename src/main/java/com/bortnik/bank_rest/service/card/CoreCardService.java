package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.CardBlocked;
import com.bortnik.bank_rest.exception.card.CardExpired;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import com.bortnik.bank_rest.repository.CardRepository;
import com.bortnik.bank_rest.service.UserService;
import com.bortnik.bank_rest.util.mappers.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Базовый сервис для работы с картами.
 * Содержит общие методы, используемые в сервисах администратора и пользователя.
 */
@Service
@RequiredArgsConstructor
public class CoreCardService {

    public final CardRepository cardRepository;
    public final UserService userService;

    /**
     * Получение всех карт пользователя с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя
     */
    public Page<CardDTO> findAllUserCards(final UUID userId, final Pageable pageable) {
        if (!userService.existsById(userId)) {
            throw new UserNotFound("User with ID " + userId + " not found");
        }
        return cardRepository.findAllByUserId(userId, pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Получение всех карт пользователя по статусу с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param status статус карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя по статусу
     */
    public Page<CardDTO> findCardsByUserIdAndStatus(
            final UUID userId,
            final CardStatus status,
            final Pageable pageable
    ) {
        if (!userService.existsById(userId)) {
            throw new UserNotFound("User with ID " + userId + " not found");
        }
        return cardRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Валидация активности карты.
     * @param card карта для проверки
     * @throws CardBlocked если карта заблокирована
     * @throws CardExpired если карта истекла
     */
    public void validateActiveCard(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardBlocked("Card with ID " + card.getId() + " is blocked");
        }
        else if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpired("Card with ID " + card.getId() + " is expired");
        }
    }
}
