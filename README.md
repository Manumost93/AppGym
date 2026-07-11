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
- **Datos**: PostgreSQL (una base de datos por servicio) + Redis (rate limiting, blacklist de tokens).
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
| Administrador de negocio | `owner@appgym.demo` | Gestiona su box: planes, staff, actividades. |
| Staff | `staff@appgym.demo` | Gestiona actividades y horarios del negocio. |
| Socio | `member@appgym.demo` | Reserva clases/pistas y usa el asistente de IA. |

Ya existen un negocio demo ("AppGym Demo Box"), 2 planes de membresía, 2 actividades (clase WOD + pista de pádel) con varias franjas horarias, y una reserva confirmada de ejemplo.

## Documentación

- [docs/architecture.md](docs/architecture.md) — decisiones de arquitectura y por qué.
- [docs/deployment.md](docs/deployment.md) — despliegue en producción (VPS + Docker Compose + Traefik + Let's Encrypt).

## Portfolio

Proyecto enlazado desde [mi portfolio](https://portfolio-kohl-seven-15tnfk4ujq.vercel.app/).
