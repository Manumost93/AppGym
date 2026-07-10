FROM node:24-alpine AS build
WORKDIR /workspace
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Configuracion "development" a proposito: este Dockerfile es para el stack local de
# docker-compose (gateway publicado en localhost:8080), no para el despliegue en Vercel,
# que usa su propio build de produccion con el dominio real (ver environment.prod.ts).
RUN npx ng build --configuration=development

FROM nginx:1.27-alpine AS runtime
COPY --from=build /workspace/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
