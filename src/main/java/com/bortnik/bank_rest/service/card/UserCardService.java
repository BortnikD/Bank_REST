package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.dto.card.CardTransactionDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.*;
import com.bortnik.bank_rest.exception.security.AccessError;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import com.bortnik.bank_rest.repository.CardRepository;
import com.bortnik.bank_rest.service.UserService;
import com.bortnik.bank_rest.util.mappers.CardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для управления картами пользователей.
 */
@Service
@RequiredArgsConstructor
public class UserCardService {

    private final CardRepository cardRepository;
    private final CoreCardService coreCardService;
    private final UserService userService;

    /**
     * Получение всех карт пользователя с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя
     */
    public Page<CardDTO> getAllUserCards(final UUID userId, final Pageable pageable) {
        validateUserExists(userId);
        return coreCardService.findAllUserCards(userId, pageable);
    }

    /**
     * Переводит указанную сумму денег между двумя картами, принадлежащими одному и тому же пользователю.
     * Подтверждает, что пользователь владеет обеими картами и что на исходной карте достаточный баланс.
     *
     * @param transactionDTO объект, содержащий детали транзакции, включая идентификатор пользователя,
     * @throws InsufficientFunds если на исходной карте недостаточно средств
     */
    @Transactional
    public void internalTransfer(final CardTransactionDTO transactionDTO, final UUID userId) {
        validateUserExists(userId);
        if (transactionDTO.getFromCardId().equals(transactionDTO.getToCardId())) {
            throw new CardsAreTheSame("Cards are can't be the same");
        }
        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IncorrectAmount("Amount must be positive");
        }

        final Card fromCard = getCardOwnedByUser(userId, transactionDTO.getFromCardId());
        coreCardService.validateActiveCard(fromCard);
        final Card toCard = getCardOwnedByUser(userId, transactionDTO.getToCardId());
        coreCardService.validateActiveCard(toCard);

        if (transactionDTO.getAmount().compareTo(fromCard.getBalance()) > 0) {
            throw new InsufficientFunds("Insufficient funds on card " + transactionDTO.getFromCardId());
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transactionDTO.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transactionDTO.getAmount()));
    }

    /**
     * Заморозка карты по номеру, пользователем.
     * Проверяет принадлежность карты пользователю.
     * @param userId ID пользователя запросившего блокировку
     * @param cardId ID карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO blockCard(
            final UUID userId,
            final UUID cardId
    ) {
        validateUserExists(userId);
        final Card card = getCardOwnedByUser(userId, cardId);
        if (card.getStatus().equals(CardStatus.BLOCKED)) {
            throw new CardAlreadyBlocked("Card is already blocked");
        }
        else if (card.getStatus().equals(CardStatus.EXPIRED)) {
            throw new CardExpired("card is expired");
        }
        card.setStatus(CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());
        return CardMapper.toCardDTO(card);
    }

    /**
     * Получение карты по номеру, пользователем.
     * Проверяет принадлежность карты пользователю.
     * @param userId ID пользователя запросившего карту
     * @param cardId ID карты
     * @return информация о карте
     */
    public CardDTO getUserCardById(
            final UUID userId,
            final UUID cardId
    ) {
        validateUserExists(userId);
        final Card card = getCardOwnedByUser(userId, cardId);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Получение всех карт пользователя по статусу с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param status статус карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя по статусу
     */
    public Page<CardDTO> getCardsByUserIdAndStatus(
            final UUID userId,
            final CardStatus status,
            final Pageable pageable
    ) {
        validateUserExists(userId);
        return coreCardService.findCardsByUserIdAndStatus(userId, status, pageable);
    }

    /**
     * Получение карты по номеру, принадлежащей пользователю.
     * @param userId ID пользователя
     * @param cardId ID карты
     * @return информация о карте
     * @throws CardNotFound если карта не найдена
     * @throws AccessError если карта не принадлежит пользователю
     */
    private Card getCardOwnedByUser(
            final UUID userId,
            final UUID cardId
    ) {
        validateUserExists(userId);
        final Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFound("Card with number " + cardId + " not found"));

        if (!card.getUserId().equals(userId)) {
            throw new AccessError("User with ID " + userId + " does not own card with number " + cardId);
        }

        return card;
    }

    private void validateUserExists(final UUID userId) {
        if (!userService.existsById(userId)) {
            throw new UserNotFound("User with ID " + userId + " not found");
        }
    }
}