package com.appgym.common.demo;

import java.util.UUID;

/**
 * IDs fijos compartidos por los seeders de datos de demostracion de
 * auth-service, business-service y booking-service. Al no haber claves
 * foraneas entre bases de datos de distintos microservicios, cada seeder
 * referencia estas mismas constantes para que los datos encajen entre si
 * (p. ej. el negocio demo de business-service es el mismo business_id que
 * llevan los usuarios demo de auth-service).
 *
 * Solo se usan si appgym.seed.enabled=true (por defecto), pensado para que
 * un visitante del portfolio pueda entrar con credenciales de demostracion
 * sin tener que registrarse. Ver README.md para las credenciales.
 */
public final class DemoSeedIds {

    public static final UUID BUSINESS_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID SUPER_ADMIN_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID BUSINESS_ADMIN_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID STAFF_USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID MEMBER_USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    public static final String DEMO_PASSWORD = "Demo1234!";

    public static final String SUPER_ADMIN_EMAIL = "admin@appgym.demo";
    public static final String BUSINESS_ADMIN_EMAIL = "owner@appgym.demo";
    public static final String STAFF_EMAIL = "staff@appgym.demo";
    public static final String MEMBER_EMAIL = "member@appgym.demo";

    private DemoSeedIds() {
    }
}
