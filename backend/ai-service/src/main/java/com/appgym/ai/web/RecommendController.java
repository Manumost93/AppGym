package com.appgym.ai.web;

import com.appgym.ai.service.RecommendationService;
import com.appgym.ai.web.dto.RecommendationResponse;
import com.appgym.common.dto.Role;
import com.appgym.common.security.AccessControl;
import com.appgym.common.security.JwtClaims;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/recommend")
public class RecommendController {

    private final RecommendationService recommendationService;

    public RecommendController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public RecommendationResponse recommend(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @RequestHeader(JwtClaims.HEADER_USER_ID) UUID memberId) {
        AccessControl.requireRole(role, Role.MEMBER);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return recommendationService.recommend(ownBusinessId, memberId);
    }
}
