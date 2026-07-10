package com.appgym.ai.client;

import com.appgym.common.dto.BookingStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingSummary(String activityName, Instant slotStartTime, BookingStatus status) {
}
