# Déploiement Production — Activ Education

## Prérequis

- VPS LWS KVM (minimum 2 vCPU, 4 Go RAM, 50 Go NVMe)
- Docker et Docker Compose installés
- Nom de domaine (ex: activeducation.tg)
- Certificats SSL (Let's Encrypt)

## 1. Connexion au serveur

```bash
ssh root@IP_DU_VPS
```

## 2. Installer Docker

```bash
curl -fsSL https://get.docker.com | sh
apt install -y docker-compose-plugin
```

## 3. Cloner le projet

```bash
git clone <url-du-repo> /opt/activ-education
cd /opt/activ-education/activ-education-backend-main
```

## 4. Configurer les variables d'environnement

```bash
cp .env.prod.example .env.prod
nano .env.prod
# Remplir toutes les clés (DB_PASSWORD, JWT_SECRET, OPENAI_API_KEY...)
```

**Générer une clé JWT sécurisée :**

```bash
openssl rand -base64 64
# Copier le résultat dans JWT_SECRET
```

## 5. Obtenir les certificats SSL

```bash
apt install -y certbot
certbot certonly --standalone -d activeducation.tg -d www.activeducation.tg
mkdir -p ssl
cp /etc/letsencrypt/live/activeducation.tg/fullchain.pem ssl/cert.pem
cp /etc/letsencrypt/live/activeducation.tg/privkey.pem ssl/key.pem
```

## 6. Build le frontend React

```bash
cd ../activ-education-fronted-main/backoffice
npm install
npm run build
# Les fichiers statiques sont dans dist/
```

## 7. Lancer la stack

```bash
cd /opt/activ-education/activ-education-backend-main
docker compose -f docker-compose.prod.yml up -d --build
```

## 8. Vérifier

```bash
docker compose -f docker-compose.prod.yml ps
curl http://localhost:8080/actuator/health
curl https://activeducation.tg/api/v1/bibliotheque/etablissements
```

## Commandes utiles

```bash
# Voir les logs
docker compose -f docker-compose.prod.yml logs -f

# Redémarrer un service
docker compose -f docker-compose.prod.yml restart app

# Mise à jour (après git pull)
docker compose -f docker-compose.prod.yml up -d --build app

# Sauvegarde BDD
docker exec activeducation-db pg_dump -U postgres activ_education > backup_$(date +%Y%m%d).sql

# Renouvellement SSL auto (cron)
echo "0 3 * * * certbot renew --quiet && docker restart activeducation-nginx" | crontab -
```
