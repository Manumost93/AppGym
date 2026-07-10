package com.appgym.ai.client;

import com.appgym.common.dto.ActivityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityInfo(ActivityType type, String name, String instructorName, boolean active) {
}
