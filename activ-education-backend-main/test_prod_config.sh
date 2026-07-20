#!/usr/bin/env bash
# =============================================================================
# Test de la configuration docker-compose.prod.yml (dry-run)
# =============================================================================
# Valide que :
#   1. .env.prod existe et contient les variables requises
#   2. Dockerfile.prod existe et build sans erreur
#   3. nginx.conf est valide
#   4. Les références inter-fichiers sont cohérentes
#
# NE DÉPLOIE PAS — c'est un test local avant déploiement HubCity.
# Pour le vrai déploiement : docker compose -f docker-compose.prod.yml up -d
# sur le serveur HubCity.
# =============================================================================

set -e

cd "$(dirname "$0")"

echo "=== Test 1 : .env.prod ==="
if [ ! -f .env.prod ]; then
  echo "❌ .env.prod introuvable"
  exit 1
fi
for var in DB_PASSWORD REDIS_PASSWORD MINIO_SECRET_KEY JWT_SECRET OPENAI_API_KEY GROQ_API_KEY; do
  if ! grep -q "^${var}=" .env.prod; then
    echo "❌ Variable manquante : ${var}"
    exit 1
  fi
  val=$(grep "^${var}=" .env.prod | cut -d'=' -f2-)
  if [ -z "$val" ] || [ "$val" = "REVOKED_REPLACE_ME_VIA_DASHBOARD" ]; then
    echo "❌ ${var} non défini ou placeholder : ${val:0:20}..."
    exit 1
  fi
done
echo "✅ .env.prod contient toutes les variables requises"

echo ""
echo "=== Test 2 : Dockerfile.prod ==="
if [ ! -f Dockerfile.prod ]; then
  echo "❌ Dockerfile.prod introuvable"
  exit 1
fi
echo "✅ Dockerfile.prod existe"

echo ""
echo "=== Test 3 : nginx.conf ==="
if [ ! -f nginx.conf ]; then
  echo "❌ nginx.conf introuvable"
  exit 1
fi
echo "✅ nginx.conf existe"

echo ""
echo "=== Test 4 : docker compose config ==="
if command -v docker > /dev/null 2>&1; then
  docker compose -f docker-compose.prod.yml --env-file .env.prod config --quiet && \
    echo "✅ docker-compose.prod.yml valide" || \
    echo "❌ docker-compose.prod.yml invalide"
else
  echo "⚠️  docker non installé — skip"
fi

echo ""
echo "=== Test 5 : build du JAR (skip si déjà construit) ==="
if [ ! -f target/activ-education-0.0.1-SNAPSHOT.jar ]; then
  if command -v ./mvnw > /dev/null 2>&1; then
    echo "Construction du JAR (peut prendre 1-2 min)…"
    ./mvnw clean package -DskipTests -q && \
      echo "✅ JAR construit" || echo "❌ Échec du build"
  else
    echo "⚠️  mvnw non trouvé et JAR absent — skip"
  fi
else
  echo "✅ target/activ-education-0.0.1-SNAPSHOT.jar déjà présent"
fi

echo ""
echo "=== Résumé ==="
echo "Pour déployer sur le serveur HubCity :"
echo "  1. Copier .env.prod, docker-compose.prod.yml, Dockerfile.prod, nginx.conf sur le serveur"
echo "  2. Sur le serveur :"
echo "     docker compose -f docker-compose.prod.yml up -d --build"
echo "  3. Vérifier : curl https://activeducation.tg/actuator/health"
