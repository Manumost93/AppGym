package com.appgym.booking.web.dto;

import com.appgym.common.dto.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActivityRequest(
        @NotNull ActivityType type,
        @NotBlank String name,
        String description,
        @NotNull @Positive Integer capacity,
        @NotNull @Positive Integer durationMinutes,
        String instructorName,
        /** Solo se tiene en cuenta al actualizar (permite reactivar una actividad desactivada); se ignora al crear. */
        Boolean active
) {
}
