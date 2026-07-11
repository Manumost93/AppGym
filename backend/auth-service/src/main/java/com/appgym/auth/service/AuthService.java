package com.appgym.auth.service;

import com.appgym.auth.domain.User;
import com.appgym.auth.domain.UserStatus;
import com.appgym.auth.repository.UserRepository;
import com.appgym.auth.web.dto.AuthResponse;
import com.appgym.auth.web.dto.LoginRequest;
import com.appgym.auth.web.dto.RegisterRequest;
import com.appgym.auth.web.dto.UpdateClientRequest;
import com.appgym.auth.web.dto.UserResponse;
import com.appgym.common.dto.Role;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Los socios (MEMBER) que se registran para unirse a un negocio quedan en
     * PENDING hasta que el BUSINESS_ADMIN de ese negocio los acepta: no reciben
     * tokens todavia, solo confirmacion de que la solicitud se ha enviado. El
     * resto de roles (altas de staff/administracion) se activan de inmediato,
     * como hasta ahora.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }
        if (request.role() != Role.SUPER_ADMIN && request.businessId() == null) {
            throw new IllegalArgumentException("businessId es obligatorio para el rol " + request.role());
        }

        User user = new User(
                UUID.randomUUID(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                request.role(),
                request.businessId()
        );

        if (request.role() == Role.MEMBER) {
            user.setStatus(UserStatus.PENDING);
        }

        userRepository.save(user);

        if (user.getStatus() == UserStatus.PENDING) {
            return new AuthResponse(null, null, 0, UserResponse.from(user));
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(User::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AccountNotActiveException("ACCOUNT_PENDING",
                    "Tu solicitud esta pendiente de aprobacion por el negocio. Te avisaremos en cuanto la revisen.");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new AccountNotActiveException("ACCOUNT_REJECTED",
                    "Tu solicitud ha sido rechazada. Contacta con el negocio para mas informacion.");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        UUID userId = refreshTokenService.consume(rawRefreshToken);
        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("Usuario no encontrado"));

        return buildAuthResponse(user);
    }

    /** Clientes (MEMBER) del negocio del BUSINESS_ADMIN que hace la peticion. */
    @Transactional(readOnly = true)
    public List<UserResponse> listClients(UUID businessId) {
        return userRepository.findByBusinessIdAndRole(businessId, Role.MEMBER).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateClient(UUID businessId, UUID clientId, UpdateClientRequest request) {
        User client = findClientOfBusiness(businessId, clientId);
        if (request.status() != null) {
            client.setStatus(request.status());
        }
        if (request.paid() != null) {
            client.setPaid(request.paid());
        }
        return UserResponse.from(client);
    }

    @Transactional
    public void deleteClient(UUID businessId, UUID clientId) {
        User client = findClientOfBusiness(businessId, clientId);
        userRepository.delete(client);
    }

    private User findClientOfBusiness(UUID businessId, UUID clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        if (client.getRole() != Role.MEMBER || !businessId.equals(client.getBusinessId())) {
            throw new NoSuchElementException("Cliente no encontrado");
        }
        return client;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user.getId());
        return new AuthResponse(accessToken, refreshToken, jwtService.accessTokenExpirationSeconds(), UserResponse.from(user));
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("Ya existe un usuario registrado con el email " + email);
        }
    }

    public static class AccountNotActiveException extends RuntimeException {
        private final String code;

        public AccountNotActiveException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
