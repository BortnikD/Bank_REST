package com.bortnik.bank_rest.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${spring.security.jwt.secret}")
    private String jwtToken;

    @Value("${spring.security.jwt.expiration}")
    private long expiration;

    private Key secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtToken);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

        /**
         * Генерация JWT-токена с указанием имени пользователя и ролей
         *
         * @param username имя пользователя
         * @param roles список ролей пользователя
         * @return сгенерированный JWT-токен
         */
        public String generateToken(String username, Collection<String> roles) {
            Claims claims = Jwts.claims().setSubject(username);
            claims.put("roles", roles);

            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + expiration);

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expirationDate)
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        }

    /**
     * Извлечение имени пользователя из токена
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Извлечение ролей пользователя из токена
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof Collection<?>) {
            return ((Collection<?>) rolesObject)
                    .stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Извлечение токена из заголовка Authorization
     */
    public Optional<String> resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    /**
     * Проверка валидности токена
     */
    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Expired or invalid JWT token");
        }
    }

    /**
     * Парсинг Claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
