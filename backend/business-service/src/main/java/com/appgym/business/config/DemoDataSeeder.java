package com.appgym.business.config;

import com.appgym.business.domain.Business;
import com.appgym.business.domain.MembershipPlan;
import com.appgym.business.repository.BusinessRepository;
import com.appgym.business.repository.MembershipPlanRepository;
import com.appgym.common.demo.DemoSeedIds;
import com.appgym.common.dto.BusinessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Crea el negocio de demostracion (mismo business_id que los usuarios demo
 * de auth-service) con un par de planes de membresia. Ver DemoDataSeeder de
 * auth-service para el resto de las piezas de este mismo dataset.
 */
@Component
@ConditionalOnProperty(value = "appgym.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final BusinessRepository businessRepository;
    private final MembershipPlanRepository membershipPlanRepository;

    public DemoDataSeeder(BusinessRepository businessRepository, MembershipPlanRepository membershipPlanRepository) {
        this.businessRepository = businessRepository;
        this.membershipPlanRepository = membershipPlanRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (businessRepository.existsById(DemoSeedIds.BUSINESS_ID)) {
            return;
        }

        Business business = new Business(
                DemoSeedIds.BUSINESS_ID,
                "AppGym Demo Box",
                BusinessType.CROSSFIT_BOX,
                "Box de crossfit de demostracion para el portfolio de AppGym.",
                "hola@appgym.demo",
                "+34 600 000 000",
                "Calle de Ejemplo 1, Madrid",
                "#10b981"
        );
        businessRepository.save(business);

        membershipPlanRepository.save(new MembershipPlan(DemoSeedIds.BUSINESS_ID, "Mensual Ilimitado",
                "Acceso libre a todas las clases durante 30 dias.", 4900, "EUR", 30));
        membershipPlanRepository.save(new MembershipPlan(DemoSeedIds.BUSINESS_ID, "Bono 10 clases",
                "10 clases a elegir, validas durante 60 dias.", 6900, "EUR", 60));

        log.info("Datos de demostracion sembrados: 1 negocio + 2 planes de membresia");
    }
}
