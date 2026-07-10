package com.appgym.common.dto;

/**
 * Roles de usuario compartidos por todos los microservicios.
 * SUPER_ADMIN no pertenece a ningun business_id (gestiona la plataforma).
 */
public enum Role {
    SUPER_ADMIN,
    BUSINESS_ADMIN,
    STAFF,
    MEMBER
}
