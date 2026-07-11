package com.appgym.auth.config;

import com.appgym.auth.domain.User;
import com.appgym.auth.domain.UserStatus;
import com.appgym.auth.repository.UserRepository;
import com.appgym.common.demo.DemoSeedIds;
import com.appgym.common.dto.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea, en el primer arranque, un usuario de cada rol con credenciales
 * conocidas (ver README.md) para que un visitante del portfolio pueda entrar
 * directamente sin registrarse. Idempotente: si el usuario admin demo ya
 * existe, no hace nada. Desactivable con appgym.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(value = "appgym.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DemoSeedIds.SUPER_ADMIN_EMAIL)) {
            return;
        }

        String hash = passwordEncoder.encode(DemoSeedIds.DEMO_PASSWORD);

        userRepository.save(new User(DemoSeedIds.SUPER_ADMIN_USER_ID, DemoSeedIds.SUPER_ADMIN_EMAIL, hash,
                "Admin Demo", Role.SUPER_ADMIN, null));
        userRepository.save(new User(DemoSeedIds.BUSINESS_ADMIN_USER_ID, DemoSeedIds.BUSINESS_ADMIN_EMAIL, hash,
                "Dueno Demo (Crossfit)", Role.BUSINESS_ADMIN, DemoSeedIds.BUSINESS_ID));
        userRepository.save(new User(DemoSeedIds.GYM_ADMIN_USER_ID, DemoSeedIds.GYM_ADMIN_EMAIL, hash,
                "Dueno Demo (Gimnasio)", Role.BUSINESS_ADMIN, DemoSeedIds.BUSINESS_ID_GYM));
        userRepository.save(new User(DemoSeedIds.PADEL_ADMIN_USER_ID, DemoSeedIds.PADEL_ADMIN_EMAIL, hash,
                "Dueno Demo (Padel)", Role.BUSINESS_ADMIN, DemoSeedIds.BUSINESS_ID_PADEL));
        userRepository.save(new User(DemoSeedIds.STAFF_USER_ID, DemoSeedIds.STAFF_EMAIL, hash,
                "Entrenador Demo", Role.STAFF, DemoSeedIds.BUSINESS_ID));
        userRepository.save(new User(DemoSeedIds.MEMBER_USER_ID, DemoSeedIds.MEMBER_EMAIL, hash,
                "Socio Demo", Role.MEMBER, DemoSeedIds.BUSINESS_ID));

        User pendingMember = new User(DemoSeedIds.PENDING_MEMBER_USER_ID, DemoSeedIds.PENDING_MEMBER_EMAIL, hash,
                "Solicitante Demo", Role.MEMBER, DemoSeedIds.BUSINESS_ID);
        pendingMember.setStatus(UserStatus.PENDING);
        userRepository.save(pendingMember);

        log.info("Datos de demostracion sembrados: 7 usuarios (ver README.md para las credenciales)");
    }
}
