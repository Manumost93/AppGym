#!/bin/bash
# Crea una base de datos y un usuario propio por microservicio (database-per-service logico).
# Se ejecuta automaticamente por la imagen oficial de Postgres en el primer arranque del volumen.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER auth_user WITH PASSWORD '${AUTH_DB_PASSWORD}';
    CREATE DATABASE auth_db OWNER auth_user;

    CREATE USER business_user WITH PASSWORD '${BUSINESS_DB_PASSWORD}';
    CREATE DATABASE business_db OWNER business_user;

    CREATE USER booking_user WITH PASSWORD '${BOOKING_DB_PASSWORD}';
    CREATE DATABASE booking_db OWNER booking_user;
EOSQL
