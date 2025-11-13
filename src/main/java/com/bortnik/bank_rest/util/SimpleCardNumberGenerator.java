package com.bortnik.bank_rest.util;

import java.security.SecureRandom;

public final class SimpleCardNumberGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CARD_LENGTH = 16;

    /**
     * Генерирует случайный номер карты длиной 16 цифр.
     * @return сгенерированный номер карты
     */
    public static String generate() {
        StringBuilder sb = new StringBuilder(CARD_LENGTH);
        for (int i = 0; i < CARD_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Маскирует номер карты, оставляя видимыми только последние четыре цифры.
     * @param cardNumber номер карты
     * @return замаскированный номер карты
     */
    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return cardNumber;
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
