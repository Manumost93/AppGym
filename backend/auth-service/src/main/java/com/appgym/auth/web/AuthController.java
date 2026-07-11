package com.appgym.auth.web;

import com.appgym.auth.repository.UserRepository;
import com.appgym.auth.service.AuthService;
import com.appgym.auth.web.dto.AuthResponse;
import com.appgym.auth.web.dto.LoginRequest;
import com.appgym.auth.web.dto.RefreshRequest;
import com.appgym.auth.web.dto.RegisterRequest;
import com.appgym.auth.web.dto.UpdateClientRequest;
import com.appgym.auth.web.dto.UserResponse;
import com.appgym.common.dto.Role;
import com.appgym.common.security.AccessControl;
import com.appgym.common.security.JwtClaims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    /**
     * Protegido por api-gateway: solo se alcanza con un JWT valido, que ya viene
     * traducido a la cabecera X-User-Id de confianza.
     */
    @GetMapping("/me")
    public UserResponse me(@RequestHeader(JwtClaims.HEADER_USER_ID) UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    /**
     * Gestion de clientes (socios) del propio negocio: solo BUSINESS_ADMIN,
     * y solo sobre los MEMBER de su mismo business_id (X-Business-Id de confianza).
     */
    @GetMapping("/clients")
    public List<UserResponse> listClients(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        return authService.listClients(AccessControl.requireBusinessId(businessId));
    }

    @PatchMapping("/clients/{id}")
    public UserResponse updateClient(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @PathVariable UUID id,
            @RequestBody UpdateClientRequest request) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        return authService.updateClient(AccessControl.requireBusinessId(businessId), id, request);
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(
            @RequestHeader(JwtClaims.HEADER_ROLE) String role,
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @PathVariable UUID id) {
        AccessControl.requireRole(role, Role.BUSINESS_ADMIN);
        authService.deleteClient(AccessControl.requireBusinessId(businessId), id);
        return ResponseEntity.noContent().build();
    }
}
