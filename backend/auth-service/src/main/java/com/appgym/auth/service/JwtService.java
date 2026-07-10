package com.appgym.auth.service;

import com.appgym.auth.config.JwtProperties;
import com.appgym.auth.domain.User;
import com.appgym.common.security.JwtClaims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Emite y valida los access tokens JWT (HS256) que auth-service firma y que
 * api-gateway valida con el mismo secreto compartido.
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim(JwtClaims.EMAIL, user.getEmail())
                .claim(JwtClaims.ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey);

        if (user.getBusinessId() != null) {
            builder.claim(JwtClaims.BUSINESS_ID, user.getBusinessId().toString());
        }

        return builder.compact();
    }

    public long accessTokenExpirationSeconds() {
        return properties.accessTokenExpirationMinutes() * 60;
    }
}
