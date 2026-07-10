package com.appgym.common.security;

/**
 * Nombres de claims JWT compartidos entre auth-service (emisor) y api-gateway (validador),
 * para evitar strings magicos duplicados en ambos servicios.
 */
public final class JwtClaims {

    public static final String ROLE = "role";
    public static final String BUSINESS_ID = "businessId";
    public static final String EMAIL = "email";

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_BUSINESS_ID = "X-Business-Id";
    public static final String HEADER_ROLE = "X-Role";

    private JwtClaims() {
    }
}
