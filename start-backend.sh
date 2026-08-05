#!/bin/bash
#
# Lance le backend Spring Boot localement.
#
# Pré-requis : la DB Docker tourne (activeducation-db expose 5433 sur l'hôte).
#
# Deux modes :
#   1) dev (par défaut)         : source .env.local s'il existe, sinon fallback en dur.
#                                 Lance `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
#   2) prod-like (BACKEND_PROD=1) : rebuild JAR puis `java -jar`. Utilise les mêmes
#                                 variables dev si .env.local absent.
#
# Usage : ./start-backend.sh         (dev)
#         BACKEND_PROD=1 ./start-backend.sh   (JAR packagé)

set -e

cd "$(dirname "$0")/activ-education-backend-main"

# Charge .env.local s'il existe (DB Docker :5433 sans SSL, clés IA révoquées OK)
if [ -f .env.local ]; then
    set -a
    # shellcheck disable=SC1091
    source .env.local
    set +a
    echo "[start-backend] .env.local chargé (DB=${DB_HOST}:${DB_PORT:-5432})"
else
    # Fallback dev — aligné sur la stack Docker du projet (5433 ext → 5432 int)
    export DB_HOST=localhost
    export DB_PORT=5433
    export DB_NAME=activ_education
    export DB_USER=postgres
    export DB_PASSWORD='abalakata'
    export JPA_DDL_AUTO=update
    export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/activ_education
    export MINIO_URL=http://localhost:9000
    export MINIO_ACCESS_KEY=minio
    export MINIO_SECRET_KEY=abalakata
    export REDIS_HOST=localhost
    export REDIS_PORT=6379
    echo "[start-backend] .env.local absent — fallback dev (DB=localhost:5433)"
fi

if [ "${BACKEND_PROD:-0}" = "1" ]; then
    echo "[start-backend] Mode prod-like — rebuild + JAR"
    ./mvnw -q -DskipTests package
    exec java -jar target/activEducation-0.0.1-SNAPSHOT.jar
else
    echo "[start-backend] Mode dev — spring-boot:run (profil dev)"
    exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
fi
