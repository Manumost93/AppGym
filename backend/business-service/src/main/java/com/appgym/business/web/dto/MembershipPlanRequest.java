package com.appgym.business.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MembershipPlanRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(0) Integer priceCents,
        @NotBlank String currency,
        @NotNull @Positive Integer durationDays
) {
}
