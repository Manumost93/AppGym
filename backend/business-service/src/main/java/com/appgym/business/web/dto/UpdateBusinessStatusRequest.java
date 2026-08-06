package com.appgym.business.web.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateBusinessStatusRequest(@NotNull Boolean active) {
}
