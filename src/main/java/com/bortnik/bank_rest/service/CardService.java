package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.CardGenerationError;
import com.bortnik.bank_rest.exception.card.CardNotFound;
import com.bortnik.bank_rest.exception.card.InsufficientFunds;
import com.bortnik.bank_rest.exception.security.AccessError;
import com.bortnik.bank_rest.repository.CardRepository;
import com.bortnik.bank_rest.util.SimpleCardNumberGenerator;
import com.bortnik.bank_rest.util.mappers.CardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    /**
     * Получение всех карт пользователя с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя
     */
    public Page<CardDTO> getAllUserCards(final UUID userId, final Pageable pageable) {
        return cardRepository.findAllByUserId(userId, pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Переводит указанную сумму денег между двумя картами, принадлежащими одному и тому же пользователю.
     * Подтверждает, что пользователь владеет обеими картами и что на исходной карте достаточный баланс.
     *
     * @param userId ID пользователя запросившего карты
     * @param fromCardNumber номер исходной карты, с которой будут списаны средства
     * @param toCardNumber номер целевой карты, на которую будут зачислены средства
     * @param amount количество денег для перевода
     * @throws InsufficientFunds если на исходной карте недостаточно средств
     */
    @Transactional
    public void internalTransfer(
            final UUID userId,
            final String fromCardNumber,
            final String toCardNumber,
            final BigDecimal amount
    ) {
        final Card fromCard = getCardOwnedByUser(userId, fromCardNumber);
        final Card toCard = getCardOwnedByUser(userId, toCardNumber);

        if (amount.compareTo(fromCard.getBalance()) > 0) {
            throw new InsufficientFunds("Insufficient funds on card " + fromCardNumber);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
    }

    /**
     * Заморозка карты по номеру, пользователем.
     * Проверяет принадлежность карты пользователю.
     * @param userId ID пользователя запросившего блокировку
     * @param cardNumber номер карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO blockCard(
            final UUID userId,
            final String cardNumber
    ) {
        final Card card = getCardOwnedByUser(userId, cardNumber);
        card.setStatus(CardStatus.BLOCKED);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Блокирует карту по номеру, администратором.
     * @param cardNumber номер карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO blockCardByAdmin(final String cardNumber) {
        final Card card = getCardByNumber(cardNumber);
        card.setStatus(CardStatus.BLOCKED);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Активирует карту по номеру, администратором.
     * @param cardNumber номер карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO activateCardByAdmin(final String cardNumber) {
        final Card card = getCardByNumber(cardNumber);
        card.setStatus(CardStatus.ACTIVE);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Удаление карты по номеру, администратором.
     * @param cardNumber номер карты
     * @return данные удаленной карты
     */
    @Transactional
    public CardDTO deleteCardByAdmin(final String cardNumber) {
        final Card card = getCardByNumber(cardNumber);
        cardRepository.delete(card);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Создание новой карты для пользователя, администратором.
     * @param userId ID пользователя, для которого создается карта
     * @return информация о созданной карте
     */
    @Transactional
    public CardDTO createCardForUser(final UUID userId) {
        return CardMapper.toCardDTO(
                cardRepository.save(
                        Card.builder()
                                .userId(userId)
                                .cardNumber(generateCardNumber())
                                .status(CardStatus.ACTIVE)
                                .balance(BigDecimal.ZERO)
                                .build()
                )
        );
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
     * Генерирует уникальный номер карты для новой карты. Метод пытается сгенерировать
     * номер карты, который в данный момент не существует в хранилище карт. Если достигнуто максимальное
     * количество попыток и уникальный номер карты не может быть сгенерирован, генерируется исключение.
     *
     * @return уникальный номер карты
     * @throws CardGenerationError если не удалось сгенерировать уникальный номер карты
     */
    private String generateCardNumber() {
        final int maxAttempts = 10;
        int attempts = 0;
        String cardNumber;
        do {
            cardNumber = SimpleCardNumberGenerator.generate();
            attempts++;
        } while (cardRepository.existsByCardNumber(cardNumber) && attempts < maxAttempts);
        if (attempts >= maxAttempts) {
            throw new CardGenerationError("Failed to generate unique card number after " + maxAttempts + " attempts");
        }
        return cardNumber;
    }

    /**
     * Получение карты по номеру.
     * @param cardNumber номер карты
     * @return информация о карте
     * @throws CardNotFound если карта не найдена
     */
    private Card getCardByNumber(final String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFound("Card with number " + cardNumber + " not found"));
    }

    /**
     * Получение карты по номеру, принадлежащей пользователю.
     * @param userId ID пользователя
     * @param cardNumber номер карты
     * @return информация о карте
     * @throws CardNotFound если карта не найдена
     * @throws AccessError если карта не принадлежит пользователю
     */
    private Card getCardOwnedByUser(
            final UUID userId,
            final String cardNumber
    ) {
        final Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFound("Card with number " + cardNumber + " not found"));

        if (!card.getUserId().equals(userId)) {
            throw new AccessError("User with ID " + userId + " does not own card with number " + cardNumber);
        }

        return card;
    }
}
