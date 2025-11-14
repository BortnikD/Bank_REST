package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
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
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
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
        return cardRepository.findAllByUserId(userId, pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Переводит указанную сумму денег между двумя картами, принадлежащими одному и тому же пользователю.
     * Подтверждает, что пользователь владеет обеими картами и что на исходной карте достаточный баланс.
     *
     * @param userId ID пользователя запросившего карты
     * @param fromCardId ID исходной карты, с которой будут списаны средства
     * @param toCardId ID целевой карты, на которую будут зачислены средства
     * @param amount количество денег для перевода
     * @throws InsufficientFunds если на исходной карте недостаточно средств
     */
    @Transactional
    public void internalTransfer(
            final UUID userId,
            final UUID fromCardId,
            final UUID toCardId,
            final BigDecimal amount
    ) {
        final Card fromCard = getCardOwnedByUser(userId, fromCardId);
        final Card toCard = getCardOwnedByUser(userId, toCardId);

        if (amount.compareTo(fromCard.getBalance()) > 0) {
            throw new InsufficientFunds("Insufficient funds on card " + fromCardId);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
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
        final Card card = getCardOwnedByUser(userId, cardId);
        card.setStatus(CardStatus.BLOCKED);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Блокирует карту по номеру, администратором.
     * @param cardId ID карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO blockCardByAdmin(final UUID cardId) {
        final Card card = getCardByNumber(cardId);
        card.setStatus(CardStatus.BLOCKED);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Активирует карту по номеру, администратором.
     * @param cardId ID карты
     * @return обновленная информация о карте
     */
    @Transactional
    public CardDTO activateCardByAdmin(final UUID cardId) {
        final Card card = getCardByNumber(cardId);
        card.setStatus(CardStatus.ACTIVE);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Удаление карты по номеру, администратором.
     * @param cardId номер карты
     * @return данные удаленной карты
     */
    @Transactional
    public CardDTO deleteCardByAdmin(final UUID cardId) {
        final Card card = getCardByNumber(cardId);
        cardRepository.delete(card);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Создает новую карту для указанного пользователя. Этот метод генерирует номер карты,
     * шифрует его и устанавливает дату истечения срока действия перед сохранением карты
     *
     * @param userId ID пользователя, для которого создается карта
     * @return {@link CardDTO} объект, содержащий сведения о созданной карточке
     */
    @Transactional
    public CardDTO createCardForUser(final UUID userId) {
        final String cardNumber = SimpleCardNumberGenerator.generate();;
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
     * Получение всех карт с пагинацией, администратором.
     * @param pageable параметры пагинации
     * @return страница с картами
     */
    public Page<CardDTO> getAllCards(final Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(CardMapper::toCardDTO);
    }

    /**
     * Получение карты по номеру.
     * @param cardId ID карты
     * @return информация о карте
     * @throws CardNotFound если карта не найдена
     */
    private Card getCardByNumber(final UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFound("Card with number " + cardId + " not found"));
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
        final Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFound("Card with number " + cardId + " not found"));

        if (!card.getUserId().equals(userId)) {
            throw new AccessError("User with ID " + userId + " does not own card with number " + cardId);
        }

        return card;
    }
}
