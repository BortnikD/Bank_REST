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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
     * Переводит указанную сумму денег между двумя картами, принадлежащими одному пользователю.
     * Проверяет владение обеими картами и достаточность баланса на исходной карте.
     *
     * @param transactionDTO детали транзакции (ID карт и сумма перевода)
     * @throws InsufficientFunds если на исходной карте недостаточно средств
     * @throws CardsAreTheSame если карты совпадают
     * @throws IncorrectAmount если сумма перевода некорректна
     * @throws CardNotFound если одна из карт не найдена
     * @throws UserNotFound если пользователь не найден
     * @throws AccessError если пользователь не владеет одной из карт
     */
    @Transactional
    public void internalTransfer(final CardTransactionDTO transactionDTO, final UUID userId) {
        log.info("Internal transfer requested: from={} to={} amount={} user={}",
                transactionDTO.getFromCardId(),
                transactionDTO.getToCardId(),
                transactionDTO.getAmount(),
                userId
        );

        validateUserExists(userId);
        if (transactionDTO.getFromCardId().equals(transactionDTO.getToCardId())) {
            log.warn("Transfer failed: cards are the same (cardId={})", transactionDTO.getFromCardId());
            throw new CardsAreTheSame("Cards are can't be the same");
        }
        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Transfer failed: incorrect amount {} from user {}", transactionDTO.getAmount(), userId);
            throw new IncorrectAmount("Amount must be positive");
        }

        final Card fromCard = getCardOwnedByUser(userId, transactionDTO.getFromCardId());
        coreCardService.validateActiveCard(fromCard);
        final Card toCard = getCardOwnedByUser(userId, transactionDTO.getToCardId());
        coreCardService.validateActiveCard(toCard);

        if (transactionDTO.getAmount().compareTo(fromCard.getBalance()) > 0) {
            log.warn("Transfer failed: insufficient funds on card {} (balance={}, requested={})",
                    fromCard.getId(), fromCard.getBalance(), transactionDTO.getAmount());
            throw new InsufficientFunds("Insufficient funds on card " + transactionDTO.getFromCardId());
        }

        // Изменения автоматически сохранятся благодаря @Transactional
        fromCard.setBalance(fromCard.getBalance().subtract(transactionDTO.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transactionDTO.getAmount()));

        log.info("Transfer success: {} -> {} amount={}",
                fromCard.getId(),
                toCard.getId(),
                transactionDTO.getAmount()
        );
    }

    /**
     * Заморозка карты по номеру, пользователем.
     * Проверяет принадлежность карты пользователю.
     * @param userId ID пользователя запросившего блокировку
     * @param cardId ID карты
     * @return обновленная информация о карте
     * @throws CardAlreadyBlocked если карта уже заблокирована
     * @throws CardExpired если карта просрочена
     * @throws CardNotFound если одна из карт не найдена
     * @throws UserNotFound если пользователь не найден
     * @throws AccessError если пользователь не владеет одной из карт
     */
    @Transactional
    public CardDTO blockCard(
            final UUID userId,
            final UUID cardId
    ) {
        log.info("Block card request: user={} card={}", userId, cardId);

        validateUserExists(userId);
        final Card card = getCardOwnedByUser(userId, cardId);

        if (card.getStatus() == CardStatus.BLOCKED) {
            log.warn("Block card failed: card {} already blocked", cardId);
            throw new CardAlreadyBlocked("Card is already blocked");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            log.warn("Block card failed: card {} expired", cardId);
            throw new CardExpired("card is expired");
        }

        card.setStatus(CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());

        log.info("Card {} successfully blocked by user {}", cardId, userId);

        return CardMapper.toCardDTO(card);
    }

    /**
     * Получение карты по номеру, пользователем.
     * Проверяет принадлежность карты пользователю.
     * @param userId ID пользователя запросившего карту
     * @param cardId ID карты
     * @return информация о карте
     * @throws CardNotFound если одна из карт не найдена
     * @throws UserNotFound если пользователь не найден
     * @throws AccessError если пользователь не владеет одной из карт
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
     * @throws UserNotFound если пользователь не найден
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
     * @throws UserNotFound если пользователь не найден
     */
    private Card getCardOwnedByUser(
            final UUID userId,
            final UUID cardId
    ) {
        validateUserExists(userId);
        final Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card not found: {}", cardId);
                    return new CardNotFound("Card with number " + cardId + " not found");
                });

        if (!card.getUserId().equals(userId)) {
            log.warn("Access denied: user {} does not own card {}", userId, cardId);
            throw new AccessError("User with ID " + userId + " does not own card with number " + cardId);
        }

        return card;
    }

    /**
     * Проверяет существование пользователя по его ID.
     * @param userId ID пользователя
     * @throws UserNotFound если пользователь не найден
     */
    private void validateUserExists(final UUID userId) {
        if (!userService.existsById(userId)) {
            log.warn("User not found: {}", userId);
            throw new UserNotFound("User with ID " + userId + " not found");
        }
    }
}