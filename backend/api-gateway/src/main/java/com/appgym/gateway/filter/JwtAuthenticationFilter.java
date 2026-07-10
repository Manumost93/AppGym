package com.appgym.gateway.filter;

import com.appgym.gateway.config.JwtProperties;
import com.appgym.gateway.config.SecurityProperties;
import com.appgym.common.security.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Unico punto de validacion de JWT de toda la plataforma. Las rutas publicas
 * (login/registro/refresh) pasan sin token; el resto requiere un JWT valido
 * firmado por auth-service, y sus claims se traducen a cabeceras de confianza
 * (X-User-Id, X-Role, X-Business-Id) para que los servicios internos no tengan
 * que volver a parsear ni validar el token.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final SecretKey signingKey;
    private final List<String> publicPaths;

    public JwtAuthenticationFilter(JwtProperties jwtProperties, SecurityProperties securityProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.publicPaths = securityProperties.publicPaths();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authorizationHeader.substring("Bearer ".length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            ServerHttpRequest.Builder mutatedRequest = exchange.getRequest().mutate()
                    .header(JwtClaims.HEADER_USER_ID, claims.getSubject())
                    .header(JwtClaims.HEADER_ROLE, claims.get(JwtClaims.ROLE, String.class));

            String businessId = claims.get(JwtClaims.BUSINESS_ID, String.class);
            if (businessId != null) {
                mutatedRequest.header(JwtClaims.HEADER_BUSINESS_ID, businessId);
            }

            return chain.filter(exchange.mutate().request(mutatedRequest.build()).build());
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
