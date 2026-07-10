package com.appgym.business.web;

import com.appgym.business.service.MembershipPlanService;
import com.appgym.business.web.dto.MembershipPlanRequest;
import com.appgym.business.web.dto.MembershipPlanResponse;
import com.appgym.common.dto.Role;
import com.appgym.common.security.AccessControl;
import com.appgym.common.security.JwtClaims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Planes de membresia del negocio del BUSINESS_ADMIN autenticado. El businessId
 * nunca viaja en la URL: siempre se toma de la cabecera de confianza que
 * propaga api-gateway, para que un BUSINESS_ADMIN no pueda operar sobre el
 * negocio de otro cambiando un id en la peticion.
 */
@RestController
@RequestMapping("/api/business/plans")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<MembershipPlanResponse> create(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @Valid @RequestBody MembershipPlanRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(ownBusinessId, request));
    }

    @GetMapping
    public List<MembershipPlanResponse> list(
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId) {
        return planService.listByBusiness(AccessControl.requireBusinessId(businessId));
    }

    @PutMapping("/{planId}")
    public MembershipPlanResponse update(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @PathVariable UUID planId,
            @Valid @RequestBody MembershipPlanRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return planService.update(ownBusinessId, planId, request);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> delete(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @PathVariable UUID planId) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        planService.delete(ownBusinessId, planId);
        return ResponseEntity.noContent().build();
    }
}
