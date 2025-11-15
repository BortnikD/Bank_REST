package com.bortnik.bank_rest.service;

import com.bortnik.bank_rest.dto.card.CardDTO;
import com.bortnik.bank_rest.dto.card.CardTransactionDTO;
import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.exception.card.CardBlocked;
import com.bortnik.bank_rest.exception.card.CardExpired;
import com.bortnik.bank_rest.exception.card.CardNotFound;
import com.bortnik.bank_rest.exception.card.InsufficientFunds;
import com.bortnik.bank_rest.exception.security.AccessError;
import com.bortnik.bank_rest.exception.user.UserNotFound;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardEncryptionService cardEncryptionService;
    private final UserService userService;

    // Срок действия карты в годах
    private final static int EXPIRATION_YEARS = 5;

    /**
     * Получение всех карт пользователя с пагинацией.
     * @param userId ID пользователя запросившего карты
     * @param pageable параметры пагинации
     * @return страница с картами пользователя
     */
    public Page<CardDTO> getAllUserCards(final UUID userId, final Pageable pageable) {
        if (!userService.existsById(userId)) {
            throw new UserNotFound("User with ID " + userId + " not found");
        }
        return cardRepository.findAllByUserId(userId, pageable)
                .map(CardMapper::toCardDTO);
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
        final Card fromCard = getCardOwnedByUser(userId, transactionDTO.getFromCardId());
        validateActiveCard(fromCard);
        final Card toCard = getCardOwnedByUser(userId, transactionDTO.getToCardId());
        validateActiveCard(toCard);

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
        final Card card = getCardOwnedByUser(userId, cardId);
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
        final Card card = getCardOwnedByUser(userId, cardId);
        return CardMapper.toCardDTO(card);
    }

    /**
     * Получение карты по номеру, администратором.
     * @param cardId ID карты
     * @return информация о карте
     */
    public CardDTO getCardByIdForAdmin(
            final UUID cardId
    ) {
        final Card card = getCardByNumber(cardId);
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
        card.setUpdatedAt(LocalDateTime.now());
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
        card.setUpdatedAt(LocalDateTime.now());
        return CardMapper.toCardDTO(card);
    }

    /**
     * Удаление карты по номеру, администратором.
     *
     * @param cardId номер карты
     */
    @Transactional
    public void deleteCardByAdmin(final UUID cardId) {
        final Card card = getCardByNumber(cardId);
        cardRepository.delete(card);
        CardMapper.toCardDTO(card);
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
        final Card card = getCardByNumber(cardId);
        validateActiveCard(card);
        card.setBalance(card.getBalance().add(amount));
        return CardMapper.toCardDTO(card);
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

    private void validateActiveCard(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardBlocked("Card with ID " + card.getId() + " is blocked");
        }
        else if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpired("Card with ID " + card.getId() + " is expired");
        }
    }
}
