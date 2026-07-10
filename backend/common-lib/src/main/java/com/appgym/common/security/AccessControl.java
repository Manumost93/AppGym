package com.appgym.common.security;

import com.appgym.common.dto.Role;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Comprobacion del rol de confianza propagado por api-gateway (cabecera X-Role,
 * ya validada a partir del JWT). Los servicios internos no vuelven a validar el
 * token, pero si deben decidir si el rol tiene permiso para la operacion.
 */
public final class AccessControl {

    private AccessControl() {
    }

    public static void requireRole(String actualRole, Role... allowed) {
        Set<Role> allowedSet = Set.copyOf(Arrays.asList(allowed));
        Role role = parse(actualRole);
        if (!allowedSet.contains(role)) {
            throw new ForbiddenException("Se requiere uno de los roles " + allowedSet + " (actual: " + actualRole + ")");
        }
    }

    /**
     * SUPER_ADMIN no pertenece a ningun negocio, por lo que su token no lleva
     * X-Business-Id. Los endpoints que operan sobre "mi negocio" deben rechazar
     * esa ausencia como 403 (no autorizado para este recurso), no como un 400
     * generico de cabecera obligatoria ausente.
     */
    public static UUID requireBusinessId(UUID businessId) {
        if (businessId == null) {
            throw new ForbiddenException("Esta operacion requiere un usuario asociado a un negocio");
        }
        return businessId;
    }

    public static Role parse(String rawRole) {
        try {
            return Role.valueOf(rawRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ForbiddenException("Rol no reconocido: " + rawRole);
        }
    }
}
