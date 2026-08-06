package com.appgym.business.web;

import com.appgym.business.service.BusinessService;
import com.appgym.business.web.dto.BusinessResponse;
import com.appgym.business.web.dto.CreateBusinessRequest;
import com.appgym.business.web.dto.UpdateBusinessRequest;
import com.appgym.business.web.dto.UpdateBusinessStatusRequest;
import com.appgym.common.dto.Role;
import com.appgym.common.security.AccessControl;
import com.appgym.common.security.JwtClaims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> create(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @Valid @RequestBody CreateBusinessRequest request) {
        AccessControl.requireRole(role, Role.SUPER_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(businessService.create(request));
    }

    @GetMapping
    public List<BusinessResponse> listAll(@RequestHeader(JwtClaims.HEADER_ROLE) String role) {
        AccessControl.requireRole(role, Role.SUPER_ADMIN);
        return businessService.listAll();
    }

    @GetMapping("/me")
    public BusinessResponse me(
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId) {
        return businessService.getById(AccessControl.requireBusinessId(businessId));
    }

    /** El BUSINESS_ADMIN edita el perfil de su propio negocio (nombre, contacto, marca). */
    @PutMapping("/me")
    public BusinessResponse updateMine(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @Valid @RequestBody UpdateBusinessRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        return businessService.update(AccessControl.requireBusinessId(businessId), request);
    }

    @GetMapping("/{id}")
    public BusinessResponse getById(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @PathVariable UUID id) {
        AccessControl.requireRole(role, Role.SUPER_ADMIN);
        return businessService.getById(id);
    }

    /** El SUPER_ADMIN activa/desactiva cualquier negocio de la plataforma. */
    @PatchMapping("/{id}/status")
    public BusinessResponse updateStatus(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusinessStatusRequest request) {
        AccessControl.requireRole(role, Role.SUPER_ADMIN);
        return businessService.updateStatus(id, request.active());
    }
}
