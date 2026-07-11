# Arquitectura de AppGym

Plataforma SaaS multi-negocio para gestionar gimnasios, boxes de crossfit y clubes de padel. Pieza de portfolio pensada para demostrar Angular, microservicios Java/Spring Boot, Docker e integracion de IA con un uso realista y operativo.

## Vision general

```
                        ┌─────────────────────┐
   Angular (Vercel) ───▶│     api-gateway      │  (unico servicio publico)
                        └──────────┬───────────┘
                                   │ JWT + rate limiting (Redis)
        ┌──────────────┬──────────┼──────────────┬──────────────┐
        ▼              ▼          ▼              ▼              ▼
  auth-service   business-service  booking-service  ai-service
   (JWT/roles)    (tenants/planes)  (clases/pistas)  (Claude API)
        │              │          │
        └──────────────┴──────────┴────────────▶ Postgres (una BD por servicio)
```

## Microservicios

| Servicio | Responsabilidad | Base de datos |
|---|---|---|
| `api-gateway` | Unico punto publico. Enrutamiento por rutas estaticas (DNS de docker-compose, sin service discovery), validacion de JWT, rate limiting con Redis. | — |
| `auth-service` | Registro/login, emision de JWT access+refresh (HS256), roles `SUPER_ADMIN / BUSINESS_ADMIN / STAFF / MEMBER`, `business_id` en el token para multi-tenancy. | `auth_db` |
| `business-service` | Negocios (tenants) de tipo `GYM / CROSSFIT_BOX / PADEL_CLUB`, branding, planes de membresia, altas de staff, estado de membresia simplificado. | `business_db` |
| `booking-service` | Modelo generico `Activity` (CLASS/COURT) + `ScheduleSlot` + `Booking` (CONFIRMED/CANCELLED/WAITLIST). Nucleo comun a clases de gimnasio/crossfit y reservas de pista de padel. | `booking_db` |
| `ai-service` | Wrapper sobre la API de Claude (SDK oficial Java `com.anthropic:anthropic-java`). Endpoints `/chat`, `/recommend`, `/insights`. Nunca accede a BD ajena directamente. | — |

`common-lib` es un modulo Maven compartido (DTOs, enums, utilidades) — no es un microservicio ni se despliega de forma independiente.

## Decisiones de infraestructura y por que

- **Sin Eureka**: con 5 servicios fijos, el DNS de docker-compose ya resuelve nombres de servicio. Anadir service discovery no aporta aprendizaje adicional a esta escala.
- **Sin message broker**: las llamadas REST sincronas son suficientes para 5 servicios. Un evento `booking.created` a un futuro `notification-service` queda documentado como mejora futura, no como parte del MVP.
- **Redis**: rate limiting de `/api/ai/**` en el gateway, blacklist de refresh tokens en `auth-service`, cache opcional de `/insights`.
- **Postgres**: un unico proceso con una base de datos y un usuario propio por servicio (database-per-service logico). En produccion vive dentro del propio VPS (mismo docker-compose.prod.yml) para simplificar el primer despliegue; migrar a un Postgres gestionado (Supabase u otro) mas adelante es solo un cambio de variables de entorno (`DB_HOST`/`DB_USER`/`DB_PASSWORD`), sin tocar codigo.
- **JWT HS256**: secreto compartido entre `api-gateway` y `auth-service`. RS256 queda documentado como mejora futura (rotacion de claves sin compartir secreto).
- **Testing**: JUnit5+Mockito en todos los servicios; Testcontainers solo en `booking-service` (el mas representativo por reservas/capacidad/transacciones).
- **CI/CD**: GitHub Actions compila y testea backend+frontend en cada push; en `main` ademas construye y publica las 5 imagenes del backend en GHCR (`ghcr.io/<usuario>/appgym-*`), listas para desplegar sin compilar nada en el servidor de produccion.
- **Despliegue**: backend en VPS barato (Hetzner) con `docker-compose.prod.yml` + Traefik (Let's Encrypt automatico, solo `api-gateway` es publico; ver `docs/deployment.md`). Sin dominio propio, se usa `nip.io` como DNS gratuito apuntando a la IP del VPS, con HTTPS real igualmente. Frontend Angular en Vercel. Se prefirio a un PaaS tipo Render por coste y porque demuestra mejor el dominio de Docker/orquestacion.

## Alta de socios y aprobacion

Los socios (`MEMBER`) que se registran publicamente desde la landing (eligiendo una de las 3 disciplinas) quedan en estado `PENDING` dentro de `auth-service` (columna `status` en `users`, con `ACTIVE`/`REJECTED` como resto de valores) y no reciben tokens hasta ser aceptados. El `BUSINESS_ADMIN` del negocio correspondiente gestiona sus propios socios (aceptar, rechazar, marcar pagado/no pagado, eliminar) desde `/api/auth/clients`, con el mismo patron de cabeceras de confianza (`X-Role`/`X-Business-Id`) que el resto de endpoints administrativos. Se eligio que cada administrador de negocio gestione solo a sus propios clientes (no un SUPER_ADMIN centralizado) porque encaja con el modelo multi-tenant ya existente, y que el estado de pago sea un simple booleano marcado a mano (sin pasarela de pago real) para mantener el alcance del MVP.

## IA (`ai-service`)

- Modelo configurable por endpoint via variables de entorno: `claude-haiku-4-5` para `/chat` (demo publica, coste bajo), `claude-sonnet-5` para `/recommend` e `/insights` (mas razonamiento, salida estructurada). Subible a `claude-opus-4-8` cambiando una variable de entorno si se desea maxima calidad.
- `/recommend` e `/insights` reciben datos ya agregados por `booking-service`/`business-service` via llamadas REST internas — la IA nunca consulta la base de datos directamente.
- Chat sin persistencia en el MVP: el historial de conversacion se reenvia completo desde Angular en cada peticion.

## Roadmap / mejoras futuras (fuera del MVP)

- Service discovery (Eureka) si se necesitara autoscaling horizontal real.
- Message broker (RabbitMQ) para desacoplar `booking.created` de notificaciones.
- `notification-service` (email/push) independiente.
- Pasarela de pago real (Stripe test mode) en `business-service`.
- JWT RS256 con rotacion de claves.
- Persistencia de historial de chat en `ai-service`.
