package com.appgym.common.security;

/**
 * Lanzada cuando el rol propagado por api-gateway (cabecera X-Role) no tiene
 * permiso para la operacion solicitada. Cada servicio la mapea a HTTP 403
 * en su propio GlobalExceptionHandler.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
