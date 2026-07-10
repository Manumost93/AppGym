package com.appgym.business.web.dto;

import com.appgym.business.domain.Business;
import com.appgym.common.dto.BusinessType;
import java.util.UUID;

public record BusinessResponse(
        UUID id,
        String name,
        BusinessType type,
        String description,
        String contactEmail,
        String contactPhone,
        String address,
        String primaryColor,
        boolean active
) {
    public static BusinessResponse from(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getName(),
                business.getType(),
                business.getDescription(),
                business.getContactEmail(),
                business.getContactPhone(),
                business.getAddress(),
                business.getPrimaryColor(),
                business.isActive()
        );
    }
}
