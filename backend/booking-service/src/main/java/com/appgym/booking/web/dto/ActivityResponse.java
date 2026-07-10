package com.appgym.booking.web.dto;

import com.appgym.booking.domain.Activity;
import com.appgym.common.dto.ActivityType;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID businessId,
        ActivityType type,
        String name,
        String description,
        int capacity,
        int durationMinutes,
        String instructorName,
        boolean active
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getBusinessId(),
                activity.getType(),
                activity.getName(),
                activity.getDescription(),
                activity.getCapacity(),
                activity.getDurationMinutes(),
                activity.getInstructorName(),
                activity.isActive()
        );
    }
}
