# syntax=docker/dockerfile:1
# Dockerfile compartido por los 5 microservicios del backend.
# Uso: docker build --build-arg SERVICE=auth-service -f backend/Dockerfile backend
ARG SERVICE

# El jar de Java es el mismo bytecode independientemente de la arquitectura de
# destino, asi que la compilacion se hace siempre en la arquitectura nativa del
# runner (BUILDPLATFORM) aunque se este construyendo una imagen multi-arch
# (linux/amd64 + linux/arm64, para poder desplegar en VPS ARM gratuitos como
# Oracle Cloud Free Tier). Solo la imagen final (runtime) se construye una vez
# por cada arquitectura de destino, y eso es solo copiar el jar ya compilado:
# no hace falta emulacion (QEMU) para eso.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY pom.xml .
COPY common-lib common-lib
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY business-service business-service
COPY booking-service booking-service
COPY ai-service ai-service
COPY mvnw mvnw
COPY .mvn .mvn
RUN chmod +x mvnw

ARG SERVICE
RUN ./mvnw -q -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
ARG SERVICE
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
