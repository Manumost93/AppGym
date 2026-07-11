package com.appgym.auth.web.dto;

import com.appgym.auth.domain.UserStatus;

/**
 * Actualizacion parcial de un cliente por parte del BUSINESS_ADMIN: cualquiera
 * de los dos campos puede venir a null si no se quiere tocar.
 */
public record UpdateClientRequest(
        UserStatus status,
        Boolean paid
) {
}
