package com.appgym.auth.service;

import com.appgym.auth.domain.User;
import com.appgym.auth.repository.UserRepository;
import com.appgym.auth.web.dto.AuthResponse;
import com.appgym.auth.web.dto.LoginRequest;
import com.appgym.auth.web.dto.RegisterRequest;
import com.appgym.auth.web.dto.UserResponse;
import com.appgym.common.dto.Role;
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
        userRepository.save(user);

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
}
