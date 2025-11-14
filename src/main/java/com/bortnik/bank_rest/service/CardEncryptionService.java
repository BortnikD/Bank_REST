package com.bortnik.bank_rest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CardEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKeySpec secretKey;

    public CardEncryptionService(@Value("${card.encryption.key}") String encryptionKey) {
        this.secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey),
                "AES"
        );
    }

    /**
     * Шифрует номер карты с использованием AES-GCM.
     * @param cardNumber номер карты для шифрования
     * @return зашифрованный номер карты в формате Base64
     */
    public String encrypt(String cardNumber) {
        try {
            // Генерируем случайный IV для каждого шифрования
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());

            // Объединяем IV и зашифрованные данные
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования номера карты", e);
        }
    }

    /**
     * Расшифровывает номер карты с использованием AES-GCM.
     * @param encryptedCardNumber зашифрованный номер карты в формате Base64
     * @return расшифрованный номер карты
     */
    public String decrypt(String encryptedCardNumber) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedCardNumber);

            // Разделяем IV и зашифрованные данные
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка расшифровки номера карты", e);
        }
    }
}