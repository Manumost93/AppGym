package com.appgym.booking.web;

import com.appgym.booking.service.ScheduleSlotService;
import com.appgym.booking.web.dto.ScheduleSlotRequest;
import com.appgym.booking.web.dto.ScheduleSlotResponse;
import com.appgym.common.dto.Role;
import com.appgym.common.security.AccessControl;
import com.appgym.common.security.JwtClaims;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking/slots")
public class ScheduleSlotController {

    private final ScheduleSlotService slotService;

    public ScheduleSlotController(ScheduleSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<ScheduleSlotResponse> create(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @Valid @RequestBody ScheduleSlotRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN, Role.STAFF);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.create(ownBusinessId, request));
    }

    @GetMapping
    public List<ScheduleSlotResponse> list(
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        Instant effectiveFrom = from != null ? from : Instant.now().minus(1, ChronoUnit.DAYS);
        Instant effectiveTo = to != null ? to : Instant.now().plus(14, ChronoUnit.DAYS);
        return slotService.listByRange(ownBusinessId, effectiveFrom, effectiveTo);
    }
}
