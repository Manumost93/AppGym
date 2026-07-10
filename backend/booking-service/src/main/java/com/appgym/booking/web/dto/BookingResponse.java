package com.appgym.booking.web.dto;

import com.appgym.common.dto.BookingStatus;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID slotId,
        String activityName,
        Instant slotStartTime,
        UUID memberId,
        BookingStatus status,
        Instant createdAt
) {
}
