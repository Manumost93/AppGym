package com.appgym.auth.web.dto;

import com.appgym.auth.domain.User;
import com.appgym.common.dto.Role;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        UUID businessId
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getBusinessId());
    }
}
