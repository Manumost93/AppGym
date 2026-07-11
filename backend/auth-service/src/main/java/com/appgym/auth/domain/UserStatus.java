package com.appgym.auth.domain;

/**
 * Estado de aprobacion de un socio (MEMBER). El resto de roles (SUPER_ADMIN,
 * BUSINESS_ADMIN, STAFF) se crean siempre en ACTIVE: solo los socios que se
 * registran desde la landing para unirse a un negocio pasan por PENDING.
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    REJECTED
}
