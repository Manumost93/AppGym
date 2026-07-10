package com.appgym.auth.service;

import com.appgym.auth.config.JwtProperties;
import com.appgym.auth.domain.RefreshToken;
import com.appgym.auth.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Refresh tokens opacos (no JWT): un valor aleatorio de alta entropia se entrega al cliente
 * y solo su hash SHA-256 se persiste, para que una fuga de la base de datos no exponga tokens validos.
 */
@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final JwtProperties properties;

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public String issue(UUID userId) {
        byte[] randomBytes = new byte[64];
        RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        Instant expiresAt = Instant.now().plus(properties.refreshTokenExpirationDays(), ChronoUnit.DAYS);
        repository.save(new RefreshToken(userId, hash(rawToken), expiresAt));

        return rawToken;
    }

    /**
     * Valida el refresh token, lo revoca (rotacion de un solo uso) y devuelve el userId asociado.
     */
    public UUID consume(String rawToken) {
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token invalido o caducado"));

        token.revoke();
        repository.save(token);
        return token.getUserId();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String message) {
            super(message);
        }
    }
}
