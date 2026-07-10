package com.appgym.ai.web;

import com.appgym.ai.service.InsightsService;
import com.appgym.ai.web.dto.InsightsResponse;
import com.appgym.common.dto.Role;
import com.appgym.common.security.AccessControl;
import com.appgym.common.security.JwtClaims;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    public InsightsResponse insights(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return insightsService.insights(ownBusinessId);
    }
}
