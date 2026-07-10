package com.appgym.booking.web;

import com.appgym.booking.service.BookingService;
import com.appgym.booking.web.dto.BookingResponse;
import com.appgym.booking.web.dto.CreateBookingRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reservas de un MEMBER. El member nunca especifica quien es: siempre se toma
 * de X-User-Id (JWT validado por api-gateway), asi que no puede reservar ni
 * cancelar en nombre de otro usuario.
 */
@RestController
@RequestMapping("/api/booking/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @RequestHeader(JwtClaims.HEADER_USER_ID) UUID memberId,
            @Valid @RequestBody CreateBookingRequest request) {
        AccessControl.requireRole(role, Role.MEMBER);
        UUID ownBusinessId = AccessControl.requireBusinessId(businessId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.book(ownBusinessId, memberId, request.slotId()));
    }

    @GetMapping("/me")
    public List<BookingResponse> mine(@RequestHeader(JwtClaims.HEADER_USER_ID) UUID memberId) {
        return bookingService.listMine(memberId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @RequestHeader(JwtClaims.HEADER_USER_ID) UUID memberId,
            @PathVariable UUID id) {
        bookingService.cancel(memberId, id);
        return ResponseEntity.noContent().build();
    }
}
