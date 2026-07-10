# Despliegue en producción

Guía paso a paso para desplegar el backend de AppGym en un VPS con Docker Compose + Traefik, y el frontend en Vercel. Pensada para ejecutarse una sola vez por servidor; las actualizaciones posteriores son solo `git push` (CI construye las imágenes) + un `pull`/`up` en el VPS.

## 0. Qué vas a necesitar

- Un VPS con Docker (recomendado: [Hetzner Cloud](https://www.hetzner.com/cloud) CX22, 2 vCPU / 4GB RAM, ~4.5€/mes; DigitalOcean o cualquier otro proveedor funciona igual).
- Un dominio propio apuntando a la IP del VPS, **o** usar [nip.io](https://nip.io) (gratis, sin registro) mientras tanto — ver paso 4.
- Tu `ANTHROPIC_API_KEY`.
- El repo ya tiene el CI configurado para publicar imágenes en GHCR (`ghcr.io/<tu-usuario>/appgym-*`) en cada push a `main` — no hace falta hacer nada extra para esto, ya está en `.github/workflows/ci.yml`.

## 1. Contratar el VPS

1. Crea una cuenta en Hetzner Cloud (u otro proveedor).
2. Crea un servidor: imagen **Ubuntu 24.04**, tamaño **CX22** (o el equivalente más barato con al menos 4GB de RAM — 5 microservicios Java + Postgres necesitan margen).
3. Añade tu clave SSH pública al crear el servidor (más simple y seguro que contraseña).
4. Anota la IP pública del servidor.

## 2. Preparar el servidor

Conéctate por SSH (`ssh root@TU_IP`) y ejecuta:

```bash
apt update && apt upgrade -y

# Instalar Docker Engine + Compose plugin (script oficial)
curl -fsSL https://get.docker.com | sh

# Firewall: solo SSH, HTTP y HTTPS
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
```

## 3. Hacer públicos los paquetes de GHCR (una sola vez)

Por defecto, GitHub Container Registry crea los paquetes como **privados** aunque el repo sea público. Como el VPS va a hacer `docker compose pull` sin autenticarse, hazlos públicos:

1. Ve a `https://github.com/users/<tu-usuario>/packages` (sustituye por tu usuario de GitHub).
2. Para cada paquete `appgym-api-gateway`, `appgym-auth-service`, `appgym-business-service`, `appgym-booking-service`, `appgym-ai-service` (aparecerán después del primer push a `main` que dispare el CI): entra → **Package settings** → **Change visibility** → **Public**.

Alternativa si prefieres mantenerlos privados: `docker login ghcr.io` en el VPS con un [Personal Access Token](https://github.com/settings/tokens) con permiso `read:packages`.

## 4. Elegir el dominio

**Opción A — tienes un dominio propio:** crea un registro DNS tipo `A` apuntando `api.tudominio.com` a la IP del VPS.

**Opción B — todavía no tienes dominio:** usa `nip.io`, un servicio DNS público que resuelve `<ip-con-guiones>.nip.io` a esa misma IP sin necesidad de configurar nada. Si tu VPS tiene la IP `203.0.113.10`, tu dominio de prueba es:

```
203-0-113-10.nip.io
```

Let's Encrypt emite certificados válidos para estos dominios sin problema, así que tienes HTTPS real desde ya. Cuando compres un dominio de verdad, solo cambias la variable `DOMAIN` en `.env.prod` y reinicias Traefik.

## 5. Copiar los ficheros de despliegue al VPS

Solo necesitas la carpeta `docker/` (no hace falta clonar el código fuente completo, ya que las imágenes vienen pre-construidas de GHCR):

```bash
# Desde tu maquina local
scp -r docker root@TU_IP:/opt/appgym
```

## 6. Configurar las variables de entorno

En el VPS:

```bash
cd /opt/appgym
cp .env.prod.example .env.prod
nano .env.prod
```

Rellena, como mínimo:

- `IMAGE_NAMESPACE`: tu usuario de GitHub en minúsculas.
- `POSTGRES_PASSWORD`, `AUTH_DB_PASSWORD`, `BUSINESS_DB_PASSWORD`, `BOOKING_DB_PASSWORD`, `JWT_SECRET`: genera cada uno con `openssl rand -base64 32`.
- `ANTHROPIC_API_KEY`: tu clave real.
- `DOMAIN`: el dominio del paso 4 (real o `.nip.io`).
- `ACME_EMAIL`: tu email (Let's Encrypt lo usa para avisos de expiración).
- `FRONTEND_ORIGIN`: la URL real de tu frontend en Vercel (para CORS).

## 7. Desplegar

```bash
cd /opt/appgym
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

La primera vez, Traefik tarda unos segundos en emitir el certificado de Let's Encrypt. Verifica:

```bash
# Deberia devolver {"status":"UP"} con HTTPS valido
curl https://TU_DOMINIO/actuator/health

# Si algo falla, revisa los logs de Traefik (emision del certificado)
docker compose -f docker-compose.prod.yml logs traefik --tail=50
```

## 8. Apuntar el frontend al backend real

Edita `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://TU_DOMINIO/api',
};
```

Commitea, haz push, y despliega en Vercel (o deja que el despliegue automático de Vercel lo recoja si ya está conectado al repo).

## 9. Mantenimiento

**Actualizar tras un nuevo push a `main`** (CI ya habrá publicado imágenes nuevas):

```bash
cd /opt/appgym
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

**Backup de la base de datos** (el volumen `postgres_data` contiene todo):

```bash
docker exec $(docker compose -f docker-compose.prod.yml ps -q postgres) \
  pg_dumpall -U appgym > backup-$(date +%F).sql
```

**Ver logs de un servicio:**

```bash
docker compose -f docker-compose.prod.yml logs api-gateway --tail=100 -f
```
