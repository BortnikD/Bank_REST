package com.bortnik.bank_rest.security;

import com.bortnik.bank_rest.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTests {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        String testSecret = "a2V5X2Zvcl9qd3RfZW5jb2RpbmdfdGhhdF9oYXNfMzJfYnl0ZXM=";

        setField(jwtTokenProvider, "jwtToken", testSecret);
        setField(jwtTokenProvider, "expiration", 3600000L);

        jwtTokenProvider.init();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String username = "testUser";
        List<String> roles = List.of("USER");

        String token = jwtTokenProvider.generateToken(username, roles);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
        assertIterableEquals(roles, jwtTokenProvider.getRolesFromToken(token));
    }

    @Test
    void getUsernameFromToken_shouldExtractCorrectUsername() {
        String token = jwtTokenProvider.generateToken("john_doe", List.of("USER"));
        assertEquals("john_doe", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getRolesFromToken_shouldExtractRoles() {
        List<String> roles = List.of("ADMIN");
        String token = jwtTokenProvider.generateToken("jane", roles);
        assertEquals(roles, jwtTokenProvider.getRolesFromToken(token));
    }

    @Test
    void resolveToken_shouldReturnOptionalToken() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization"))
                .thenReturn("Bearer some.jwt.token");

        Optional<String> token = jwtTokenProvider.resolveToken(request);

        assertTrue(token.isPresent());
        assertEquals("some.jwt.token", token.get());
    }

    @Test
    void resolveToken_shouldReturnEmptyIfHeaderMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization"))
                .thenReturn(null);

        Optional<String> token = jwtTokenProvider.resolveToken(request);

        assertTrue(token.isEmpty());
    }

    @Test
    void validateToken_shouldNotThrowForValidToken() {
        String token = jwtTokenProvider.generateToken("validUser", List.of("USER"));
        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldThrowForInvalidToken() {
        String invalidToken = "invalid.token.value";

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> jwtTokenProvider.validateToken(invalidToken));

        assertEquals("Expired or invalid JWT token", exception.getMessage());
    }
}
