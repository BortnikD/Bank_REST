package com.bortnik.bank_rest.service.card;

import com.bortnik.bank_rest.security.card_encryption.CardEncryptionService;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class CardEncryptionServiceTests {

    String base64Key = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes()
    );
    private final CardEncryptionService encryptionService = new CardEncryptionService(base64Key);

    @Test
    void encryptCardNumber_success() {
        String cardNumber = "1234567812345678";

        String encrypted = encryptionService.encrypt(cardNumber);

        assertNotNull(encrypted);
        assertNotEquals(cardNumber, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);

        assertEquals(cardNumber, decrypted);
    }

    @Test
    void encrypt_generatesDifferentCiphertexts() {
        String cardNumber = "1111222233334444";

        String encrypted1 = encryptionService.encrypt(cardNumber);
        String encrypted2 = encryptionService.encrypt(cardNumber);

        // AES-GCM с рандомным IV → зашифрованные тексты должны отличаться
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void decrypt_invalidBase64_throwsException() {
        assertThrows(RuntimeException.class, () ->
                encryptionService.decrypt("not-base64$$$")
        );
    }
}
