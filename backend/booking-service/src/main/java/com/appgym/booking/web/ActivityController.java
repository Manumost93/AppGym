package com.appgym.booking.web;

import com.appgym.booking.service.ActivityService;
import com.appgym.booking.web.dto.ActivityRequest;
import com.appgym.booking.web.dto.ActivityResponse;
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

@RestController
@RequestMapping("/api/booking/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> create(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @Valid @RequestBody ActivityRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN, Role.STAFF);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(ownBusinessId, request));
    }

    @GetMapping
    public List<ActivityResponse> list(
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId) {
        return activityService.listActive(AccessControl.requireBusinessId(businessId));
    }

    @PutMapping("/{id}")
    public ActivityResponse update(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN, Role.STAFF);
        return activityService.update(AccessControl.requireBusinessId(businessId), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @PathVariable UUID id) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN, Role.STAFF);
        activityService.deactivate(AccessControl.requireBusinessId(businessId), id);
        return ResponseEntity.noContent().build();
    }
}
