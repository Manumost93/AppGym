package com.appgym.common.dto;

import java.time.Instant;

/**
 * Formato de error uniforme devuelto por todos los microservicios.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path);
    }
}
