package com.appgym.gateway.config;

import com.appgym.common.security.JwtClaims;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Resuelve la clave de rate limiting por usuario autenticado (X-User-Id, ya
 * traducido por JwtAuthenticationFilter antes de que se aplique el filtro de
 * ruta), en vez de por IP: asi cada socio tiene su propio cupo sobre
 * /api/ai/** independientemente de cuantos usuarios compartan red.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(JwtClaims.HEADER_USER_ID))
                .defaultIfEmpty("anonymous");
    }
}
