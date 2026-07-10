package com.appgym.business.web.dto;

import com.appgym.business.domain.MembershipPlan;
import java.util.UUID;

public record MembershipPlanResponse(
        UUID id,
        UUID businessId,
        String name,
        String description,
        int priceCents,
        String currency,
        int durationDays,
        boolean active
) {
    public static MembershipPlanResponse from(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getBusinessId(),
                plan.getName(),
                plan.getDescription(),
                plan.getPriceCents(),
                plan.getCurrency(),
                plan.getDurationDays(),
                plan.isActive()
        );
    }
}
