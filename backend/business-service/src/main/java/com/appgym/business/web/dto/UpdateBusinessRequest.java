package com.appgym.business.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * A diferencia de CreateBusinessRequest, no incluye "type": una vez creado el
 * negocio (y con actividades/planes ya asociados a ese tipo), cambiarlo no
 * tiene sentido de negocio.
 */
public record UpdateBusinessRequest(
        @NotBlank String name,
        String description,
        @Email String contactEmail,
        String contactPhone,
        String address,
        String primaryColor
) {
}
