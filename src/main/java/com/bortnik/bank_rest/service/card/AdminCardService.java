package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.*;
import com.bortnik.bank_rest.exception.user.UserNotFound;
import com.bortnik.bank_rest.repository.CardRepository;
import com.bortnik.bank_rest.service.UserService;
import com.bortnik.bank_rest.security.card_encryption.CardEncryptionService;
import com.bortnik.bank_rest.util.SimpleCardNumberGenerator;
import com.bortnik.bank_rest.util.mappers.CardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для управления картами пользователей администратором.
 */
@Service
@RequiredArgsConstructor
public class AdminCardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final CoreCardService coreCardService;
    private final CardEncryptionService cardEncryptionService;

    // Срок действия карты в годах
    private final static int EXPIRATION_YEARS = 5;

    /**
     * Получение всех карт пользователя с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя
     */
    public Page<CardDTO> getAllUserCards(final UUID userId, final Pageable pageable) {
        return coreCardService.findAllUserCards(userId, pageable);
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
        return coreCardService.findCardsByUserIdAndStatus(userId, status, pageable);
    }

    /**
     * Получение всех карт с пагинацией, администратором.
     * @param pageable параметры пагинации
     * @return страница с картами
     */
    public Page<CardDTO> getAllCards(final Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Получение всех карт по статусу с пагинацией, администратором.
     * @param status статус карты
     * @param pageable параметры пагинации
     * @return страница с картами по статусу
     */
    public Page<CardDTO> getAllCardsByStatus(
            final CardStatus status,
            final Pageable pageable
    ) {
        return cardRepository.findByStatus(status, pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Получение карты по номеру, администратором.
     * @param cardId ID карты
     * @return информация о карте
     */
    public CardDTO getCardById(final UUID cardId) {
        final Card card = getCardEntityById(cardId);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Блокирует карту по номеру, администратором.
     * @param cardId ID карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO blockCard(final UUID cardId) {
        final Card card = getCardEntityById(cardId);
        if (card.getStatus().equals(CardStatus.BLOCKED)) {
            throw new CardAlreadyBlocked("Card is already blocked");
        }
        validateNotExpiredCard(card);
        card.setStatus(CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());
        return CardMapper.toCardDTO(card);
    }

    /**
     * Активирует карту по номеру, администратором.
     * @param cardId ID карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO activateCard(final UUID cardId) {
        final Card card = getCardEntityById(cardId);
        if (card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new CardAlreadyActivated("Card is already activated");
        }
        validateNotExpiredCard(card);
        card.setStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());
        return CardMapper.toCardDTO(card);
    }

    /**
     * Удаление карты по номеру, администратором.
     *
     * @param cardId номер карты
     */
    @Transactional
    public void deleteCard(final UUID cardId) {
        final Card card = getCardEntityById(cardId);
        cardRepository.delete(card);
    }

    /**
     * Создает новую карту для указанного пользователя. Этот метод генерирует номер карты,
     * шифрует его и устанавливает дату истечения срока действия перед сохранением карты. Только для админа
     *
     * @param userId ID пользователя, для которого создается карта
     * @return {@link CardDTO} объект, содержащий сведения о созданной карточке
     */
    @Transactional
    public CardDTO createCardForUser(final UUID userId) {
        if (!userService.existsById(userId)) {
            throw new UserNotFound("User with ID " + userId + " not found");
        }

        final String cardNumber = SimpleCardNumberGenerator.generate();
        final String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        final String encryptedCardNumber = cardEncryptionService.encrypt(cardNumber);
        final LocalDate expirationDate = LocalDate.now().plusYears(EXPIRATION_YEARS);

        return CardMapper.toCardDTO(
                cardRepository.save(
                        Card.builder()
                                .userId(userId)
                                .cardNumber(encryptedCardNumber)
                                .lastFourDigits(lastFourDigits)
                                .status(CardStatus.ACTIVE)
                                .balance(BigDecimal.ZERO)
                                .expirationDate(expirationDate)
                                .build()
                )
        );
    }

    /**
     * Пополнение баланса карты по id, админом.
     * @param cardId ID карты
     * @param amount сумма пополнения
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO topUpCardBalance(final UUID cardId, final BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IncorrectAmount("Amount must be positive");
        }
        final Card card = getCardEntityById(cardId);
        coreCardService.validateActiveCard(card);
        card.setBalance(card.getBalance().add(amount));
        return CardMapper.toCardDTO(card);
    }

    /**
     * Получение карты по номеру.
     * @param cardId ID карты
     * @return информация о карте
     * @throws CardNotFound если карта не найдена
     */
    private Card getCardEntityById(final UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFound("Card with id " + cardId + " not found"));
    }

    private void validateNotExpiredCard(final Card card) {
        if (card.getStatus().equals(CardStatus.EXPIRED)) {
            throw new CardExpired("card is expired");
        }
    }

}
