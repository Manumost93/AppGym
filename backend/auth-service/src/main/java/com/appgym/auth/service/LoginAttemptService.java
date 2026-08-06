package com.appgym.auth.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Freno de fuerza bruta sobre /login: cuenta intentos fallidos por email en
 * Redis con una ventana deslizante. No distingue "email no existe" de
 * "contrasena incorrecta" a la hora de contar, para no dar pistas sobre que
 * emails estan registrados.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "login-attempts:";

    private final StringRedisTemplate redisTemplate;

    public LoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkNotBlocked(String email) {
        String raw = redisTemplate.opsForValue().get(key(email));
        int attempts = raw != null ? Integer.parseInt(raw) : 0;
        if (attempts >= MAX_ATTEMPTS) {
            throw new TooManyAttemptsException(
                    "Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo.");
        }
    }

    public void recordFailure(String email) {
        String key = key(email);
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, WINDOW);
        }
    }

    public void recordSuccess(String email) {
        redisTemplate.delete(key(email));
    }

    private String key(String email) {
        return KEY_PREFIX + email.toLowerCase();
    }

    public static class TooManyAttemptsException extends RuntimeException {
        public TooManyAttemptsException(String message) {
            super(message);
        }
    }
}
