# AppGym

SaaS multi-negocio para gestionar **gimnasios, boxes de crossfit y clubes de pádel**: reservas con control de aforo y lista de espera, gestión de planes de membresía y staff, y un asistente con inteligencia artificial (chat, recomendador de actividades e insights de negocio) sobre la API de Claude.

Proyecto de portfolio construido con **Angular + microservicios Java/Spring Boot + Docker**, pensado para funcionar de extremo a extremo, no como un scaffold a medias.

## Arquitectura

```
                        ┌─────────────────────┐
   Angular (Vercel) ───▶│     api-gateway      │  (único servicio público)
                        └──────────┬───────────┘
                                   │ JWT + rate limiting (Redis)
        ┌──────────────┬──────────┼──────────────┬──────────────┐
        ▼              ▼          ▼              ▼              ▼
  auth-service   business-service  booking-service  ai-service
   (JWT/roles)    (tenants/planes)  (clases/pistas)  (Claude API)
        │              │          │
        └──────────────┴──────────┴────────────▶ Postgres (una BD por servicio)
```

5 microservicios Spring Boot + `api-gateway` tras JWT, más `common-lib` (módulo Maven compartido, no un servicio). Detalles de cada decisión de arquitectura en [docs/architecture.md](docs/architecture.md).

## Stack

- **Frontend**: Angular 22 (standalone components + signals) + Tailwind CSS.
- **Backend**: Java 21 + Spring Boot 3.4 (Maven multi-módulo), Spring Cloud Gateway, Spring Data JPA, JWT (HS256).
- **IA**: SDK oficial de Java para la API de Claude (`com.anthropic:anthropic-java`) — chat, recomendaciones y salidas estructuradas.
- **Datos**: PostgreSQL (una base de datos por servicio) + Redis (rate limiting real de `/api/ai/**` y freno de fuerza bruta en el login).
- **Infraestructura**: Docker Compose, GitHub Actions (test + build en cada push, publicación de imágenes en GHCR desde `main`), Traefik + Let's Encrypt en producción.

## Ejecutar en local

Requisitos: Docker y Docker Compose.

```bash
cd docker
cp .env.example .env   # y rellena ANTHROPIC_API_KEY si quieres probar el asistente de IA
docker compose up -d
```

- Frontend: http://localhost:4200
- API Gateway: http://localhost:8080
- Swagger UI de cada servicio (solo en dev): `auth-service` en `:8081/swagger-ui.html`, `business-service` en `:8082`, `booking-service` en `:8083`, `ai-service` en `:8084`.

Al arrancar por primera vez, cada servicio siembra datos de demostración automáticamente (idempotente, no se duplican en reinicios sucesivos).

## Credenciales de demostración

Contraseña común: **`Demo1234!`**

| Rol | Email | Qué puede hacer |
|---|---|---|
| Super admin | `admin@appgym.demo` | Gestiona todos los negocios de la plataforma. |
| Admin del box de crossfit | `owner@appgym.demo` | Gestiona el box: planes, clientes, staff, actividades. |
| Admin del gimnasio | `gym-owner@appgym.demo` | Gestiona el gimnasio: planes, clientes, clases. |
| Admin del club de pádel | `padel-owner@appgym.demo` | Gestiona el club: planes, clientes, pistas. |
| Staff | `staff@appgym.demo` | Gestiona actividades y horarios del box de crossfit. |
| Socio | `member@appgym.demo` | Reserva clases/pistas y usa el asistente de IA. |
| Socio pendiente | `pending@appgym.demo` | Cuenta de ejemplo con la solicitud sin aprobar todavía (no puede iniciar sesión). |

Hay 3 negocios demo, uno por disciplina, cada uno con sus propias actividades, horarios y planes de membresía: un gimnasio (spinning, yoga), un box de crossfit (WOD, halterofilia) y un club de pádel (2 pistas).

### Alta de nuevos socios y aprobación

Desde la landing, al pinchar en una de las 3 disciplinas se abre un registro con el negocio ya preseleccionado. La cuenta creada queda **pendiente de aprobación** y no puede iniciar sesión todavía. El administrador de ese negocio revisa las solicitudes en **Gestión de clientes** (`/clients`, enlazado desde su panel): puede aceptar, rechazar, marcar como pagado/no pagado o eliminar a cada socio. Una vez aceptado, el socio ya puede iniciar sesión con normalidad y se le redirige automáticamente a la página de reservas de su negocio (no hay notificaciones en tiempo real: el efecto se ve la próxima vez que el socio inicia sesión).

### Tres páginas de disciplina, cada una con su propio calendario

Cada socio, al iniciar sesión, llega a la página propia de su negocio — completamente independiente de las otras dos, aunque las tres compartan la misma mecánica de reservas con control de aforo y lista de espera:

- **`/gimnasio`**: clases dirigidas (spinning, yoga) + una sección de rutinas de sala libre sugeridas.
- **`/box`**: destaca el **próximo WOD** en una tarjeta grande con su detalle (series, repeticiones…), y debajo el resto de clases/horarios.
- **`/padel`**: selector visual de pistas que filtra el calendario al pincharlas.

Las tres reutilizan un componente compartido de calendario (`ActivityCalendarComponent`) para la lógica de reservar/cancelar, y la ruta `/schedule` actúa como despachador: redirige a cada socio a la página que le corresponde según el tipo de negocio al que pertenece.

## Gestión de cuenta y de negocio

- Cualquier usuario puede **cambiar su contraseña** y **cerrar sesión de verdad** (revoca el token en el servidor, no solo en el navegador) desde el panel principal.
- El `BUSINESS_ADMIN` puede **editar el perfil de su negocio** (nombre, descripción, contacto, color de marca) en cualquier momento desde "Mi negocio".
- El `SUPER_ADMIN` puede **activar o desactivar** cualquier negocio de la plataforma desde "Gestionar negocios".
- Las clases/pistas desactivadas se pueden **reactivar** desde "Actividades y horario" (casilla "Ver desactivadas").
- El login está protegido contra fuerza bruta (5 intentos fallidos y hay que esperar), y `/api/ai/**` tiene límite de peticiones por usuario para proteger la cuota de la API de Claude.

## Documentación

- [docs/architecture.md](docs/architecture.md) — decisiones de arquitectura y por qué.
- [docs/deployment.md](docs/deployment.md) — despliegue en producción (VPS + Docker Compose + Traefik + Let's Encrypt).

## Portfolio

Proyecto enlazado desde [mi portfolio](https://portfolio-kohl-seven-15tnfk4ujq.vercel.app/).
