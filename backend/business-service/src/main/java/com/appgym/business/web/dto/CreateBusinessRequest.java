package com.appgym.business.web.dto;

import com.appgym.common.dto.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBusinessRequest(
        @NotBlank String name,
        @NotNull BusinessType type,
        String description,
        @Email String contactEmail,
        String contactPhone,
        String address,
        String primaryColor
) {
}
