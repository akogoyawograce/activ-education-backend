# Prompt pour Claude — Aide à la rédaction du mémoire Activ Education

## Contexte

Tu aides un étudiant de l'Institut Polytechnique DEFITECH (Togo) à rédiger son mémoire de Licence Professionnelle en Génie Logiciel. Le projet s'intitule **"Conception et développement d'une plateforme intelligente d'orientation scolaire pour les élèves togolais"**.

La plateforme s'appelle **Activ Education**. Elle a été développée au sein du hub technologique **HubCity / Woélab** au Togo.

Un premier jet du mémoire a déjà été rédigé dans le fichier `memoire_activ_education.md`. Tu dois l'améliorer, le compléter et le structurer selon le canevas DEFITECH fourni dans `Caneva Redation mémoire Génie Logiciel.pdf`.

## Ce que tu dois faire

1. Lire le fichier `memoire_activ_education.md` qui contient le premier jet
2. Lire ou consulter le plan du canevas DEFITECH
3. Améliorer le document : corriger, enrichir, structurer, ajouter des détails techniques, des explications, des justifications
4. PRODUIRE UN DOCUMENT WORD (.docx) FINAL, prêt à être imprimé, avec :
   - Une mise en page professionnelle (Times New Roman 14, interligne 1.5, justifié)
   - Des emplacements clairs pour les figures (diagrammes UML, captures d'écran)
   - Une table des matières automatique
   - Des numéros de pages
   - Une bibliographie complète
   - Tous les chapitres remplis

## Informations détaillées sur le projet

### Vue d'ensemble

Activ Education est une plateforme complète d'orientation scolaire destinée aux élèves du secondaire et aux étudiants au Togo. Elle combine :
- Un **diagnostic de profil** (quiz adaptatif RIASEC + analyse OCR des bulletins)
- Une **bibliothèque orientante** (fiches établissements, filières, métiers, séries)
- Un **assistant IA** (ORIA) avec contexte RAG sur le système éducatif togolais
- Un **accompagnement humain** (rendez-vous avec conseillers, messagerie)
- Un **backoffice d'administration** (gestion des contenus, utilisateurs, statistiques)

### Architecture technique

```
Client Mobile (Flutter 3.27)  ──┐
                                │  HTTPS/JSON
Client Web Backoffice (React 19) ──┤
                                │
                                ▼
                    API REST (Spring Boot 4.0.5 / Java 21)
                    ┌──────────────────────────────────┐
                    │ Controllers → Services → Repos   │
                    │ Package by Feature :              │
                    │ profil / bibliotheque / diagnostic│
                    │ accompagnement / shared           │
                    └──────┬───────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
  PostgreSQL 16      Redis 7            MinIO (S3)
  (pgvector)         (cache,            (fichiers)
  + JPA/Hibernate     rate limiting,
                     token blacklist)
        │
        ▼
  APIs IA externes :
  OpenAI (GPT-4o-mini, text-embedding-3-small, Vision)
  Groq (Llama 3.3-70B) — fallback chat
  (OpenAI quota épuisé → fallback Groq)
```

### 5 modules du backend (Package by Feature)

1. **profil** : Utilisateurs (Élève, Parent, Conseiller, Admin), authentification JWT, 2FA, profils, documents, OCR bulletins
2. **bibliotheque** : Fiches établissements/filières/métiers/séries, FAQ, favoris, recherche vectorielle pgvector
3. **diagnostic** : Quiz adaptatif RIASEC, questions, réponses, résultats, recommandations, seuils d'admission
4. **accompagnement** : Rendez-vous, messagerie, tickets, disponibilités
5. **shared** : Sécurité (JWT, CORS, rate limiting), IA (OpenAI, ORIA), MinIO, configuration, audit logs

### 3 applications

| Application | Technologie | Entrée |
|-------------|-------------|--------|
| Backend API | Spring Boot 4.0.5 + Maven | port 8080, Swagger : `/swagger-ui.html` |
| Mobile | Flutter 3.27 (Dart, setState) | `main.dart`, API_URL dans `.env` |
| Backoffice | React 19 + TS 6 + Tailwind v4 + Vite | `main.tsx`, port 5174 |

### Base de données

- PostgreSQL 16 avec extension **pgvector** (768 dimensions, cosinus similarity)
- JPA/Hibernate `ddl-auto=update` (pas de Flyway/Liquibase)
- Héritage JOINED : Fiche abstraite → FicheEtablissement, FicheFiliere, FicheMetier, FicheSerie
- Utilisateur abstrait → Eleve, Parent, Conseiller, Administrateur
- Chiffrement : mots de passe BCrypt coût 12, JWT HS512 avec clé 512-bit

### Sécurité

- JWT stateless, access token 15min, refresh token 7 jours, blacklist Redis
- Rate limiting Redis, 2FA/TOTP (RFC 6238, HMAC-SHA1)
- CSP, HSTS, CORS configuré (origines locales + domaine production)
- `@PreAuthorize` sur tous les contrôleurs + SecurityConfig avec règles par chemin
- 4 rôles : ELEVE, PARENT, CONSEILLER, ADMIN / SUPER_ADMIN
- Fichiers upload max 20MB

### Tests

- 28 tests unitaires backend (JUnit 5 + Mockito)
- AuthServiceTest (8), AuthControllerTest (4), StatsServiceTest (4), StatsControllerTest (3), TestControllerTest (3), EleveServiceTest (6)
- Flutter : 3 tests (widget smoke, model serialization, API integration)
- Backoffice : pas de framework de test

### Fonctionnalités du CDC original

Le Cahier des Charges initial (`CDC Activ Education -2.pdf`) prévoyait :
- Authentification et profils (Élève, Parent, Conseiller, Admin)
- Quiz d'orientation adaptatif (RIASEC)
- Saisie et analyse des notes scolaires
- Bibliothèque de fiches (établissements, filières, métiers)
- Recherche et filtres
- Favoris
- Rendez-vous avec conseillers
- Messagerie entre élèves et conseillers
- Assistant virtuel (Gemini → OpenAI)
- Notifications
- Backoffice d'administration
- Statistiques et KPIs

### Fonctionnalités AJOUTÉES (au-delà du CDC)

Voici toutes les fonctionnalités ajoutées au fil des sprints qui ne figuraient pas dans le CDC initial :

1. **2FA/TOTP** (Session 1) : Authentification à deux facteurs, `TotpSecret` entity, setup/verify screens Flutter, étape TOTP dans LoginPage backoffice

2. **OCR bulletins** (Session 1) : Analyse automatique des bulletins de notes (PDFBox pour PDF, OpenAI Vision pour images), extraction matière/note/coefficient

3. **Assistant IA ORIA avec RAG** (Session 1, amélioré Session 8-9) : Chatbot avec contexte du système éducatif togolais, 289 établissements injectés dans le prompt, recherche vectorielle pgvector

4. **Recommandation IA personnalisée** (Session 1, amélioré Session 9) : Recommandations basées sur profil + notes + quiz, détection profil vide

5. **Quiz adaptatif** (Session 1) : Réordonnancement dynamique des questions par domaine sous-testé

6. **Dashboard par type d'apprenant** (Session 1) : Dashboard spécifique pour Bachelier, Décrocheur (orange, parcours pas à pas), Réconversion (CPF, VAE, bilan compétences)

7. **Audit logs** (Session 1) : `AuditLog` entity, filtre automatique POST/PUT/DELETE, AdminLogsController

8. **FAQ avec feedback** (Session 1) : Vote Utile/Pas utile sur les réponses FAQ

9. **Paramètres applicatifs configurables** (Session 1) : `ParametreApplication` entity, contrôleur, interface backoffice

10. **Import/Export CSV** (Session 1) : `CsvController`, boutons backoffice pour exporter/importer les élèves

11. **Graphe inter-fiches** (Session 1) : `LienInterFiche` entity, relations entre fiches avec CRUD controller

12. **Notifications push** (Session 7) : FCM, sauvegarde DB, fallback silencieux

13. **Rappels SMS** (Session 7) : Twilio + Orange API, `@EnableScheduling` CRON quotidien 8h00

14. **Versioning fiches (paper trail)** (Session 7) : `VersionHistorique` entity, diff JSON automatique

15. **Consentement parental** (Session 7) : Pour élèves < 15 ans, email de validation, token, IP

16. **Mode maintenance** (Session 7) : `MaintenanceFilter` statique, IP privées bypass, controller admin

17. **WYSIWYG editor** (Session 7) : `RichTextEditor.tsx` composant React contentEditable

18. **Monitoring** (Session 7) : `docker-compose.monitoring.yml` (Prometheus, Grafana, ELK)

19. **Système de tickets** (Session 7) : `Ticket` entity (OUVERT→ASSIGNE→EN_COURS→RESOLU→FERME), round-robin assignment

20. **Relevé de notes validation** (Session 7) : Upload relevé → analyse IA Groq → mise à jour automatique niveau (ex: COLLEGIEN+BEPC→LYCEEN)

21. **Persistence ORIA** (Session 8) : Sauvegarde des conversations en DB (OriaMessage entity), historique par session, GET/DELETE session

22. **Badges avec explications** (Session 9) : Dialog listant les critères de déblocage (quiz, exploration, profil, entretiens, activité)

23. **Témoignages avec aide** (Session 9) : Help dialog expliquant le fonctionnement

### Correctifs de sécurité appliqués (Session 9)

- 6 crashs Flutter runtime corrigés (substring vide, null assert, setState après dispose, toUpperCase null, initiales vides)
- @PreAuthorize ajouté sur 17 contrôleurs backend
- CSV import : génération mot de passe hash au lieu de null
- Secrets : `application-dev.properties` passe en variables d'environnement obligatoires
- Upload fichiers : 500MB → 20MB
- WebSocket : `setAllowedOrigins("*")` → `setAllowedOriginPatterns`
- Téléchargement fichiers : initialement passé en `authenticated()`, puis revenu à `permitAll()` car NetworkImage Flutter ne transmet pas de token JWT
- CORS : origines explicites (localhost, domaine production)
- Backoffice : TypeScript strict mode activé
- npm audit : 3 vulnérabilités → 0 après fix

### Session 10 — Correctifs supplémentaires et visioconférence (30 juin 2026)

#### Correctifs de code
- **40 casts safe** dans 14 services Flutter (`as List` → `as List<dynamic>? ?? []`)
- **~20 casts safe** dans `main.dart` et 4 screens Flutter (route arguments)
- **3 `substring`** sécurisés avec garde de longueur
- **Backoffice** : mock data remplacées (`trend` supprimés, badges "Démo"), `contacts.get(id)!` null-safe, URLs hardcodées → `api.defaults.baseURL`
- **Backend** : `DataLoader.java` : credentials admin injectés via `@Value` (paramétrables dans `.env`)
- **Token persistant web** : `shared_preferences` pour localStorage au lieu de RAM → plus de perte du token au rafraîchissement

#### Recommandation IA réparée
- `OpenAIEmbeddingServiceImpl.generateAnswer()` utilisait `openai.api.key` (quota OpenAI épuisé → 429) au lieu de la clé Groq
- URL et clé API lues depuis `openai.api.chat.url` et `openai.api.chat.key` (Groq) avec fallback sur `groq.api.key` puis `openai.api.key`
- La recommandation génère maintenant des réponses personnalisées via Groq (Llama 3.3-70B)

#### Visioconférence (appels Meet)
- **`VisioService.java`** : génère des liens Jitsi Meet uniques (`https://meet.jit.si/ActivEducation-{uuid}`)
- Auto-génération du lien à la création du rendez-vous dans `RendezVousServiceImpl.planifier()`
- Endpoint `POST /api/v1/rendez-vous/{trackingId}/generer-lien-visio` pour régénérer un lien
- Le champ `lienVisio` existait déjà dans l'entité `RendezVous`, les DTOs, les modèles Flutter et l'UI (boutons "Rejoindre"/"Lien visio" déjà présents)

#### Infrastructure locale
- Flutter web build déployé sur Nginx à `/var/www/activeducation-mobile/`
- Domaine local : `m.activ-education.local` (via `/etc/hosts`) accessible depuis le mobile via `*.nip.io`
- CORS backoffice : `VITE_API_BASE_URL` changé de `http://localhost:8080/api/v1` à `http://localhost/api/v1` (via Nginx proxy)

### Problèmes connus / Limitations

- OpenAI embedding quota épuisé (429) → recherche vectorielle ne fonctionne pas, fallback mot-clé LIKE utilisé
- Flutter Web : `flutter_secure_storage` ne fonctionne pas → try-catch avec fallback mémoire
- Aucun CI/CD (GitHub Actions, Jenkins)
- Tests backend limités à 28 (pas de tests pour les features ajoutées en session 7-9)
- Pas de tests de charge
- Base de données déployée vide → nécessite exécution des scripts seed
- `flutter run` sur Chrome instable (Dart compiler exit)
- `adb reverse tcp:8080 tcp:8080` nécessaire sur Android pour accéder au backend local

### Structure des fichiers importants

Le projet est organisé comme suit (2 dossiers principaux) :

**Backend :** `activ-education-backend-main/`
- `src/main/java/tg/edtch/activEducation/` : code source Java
  - `profil/` : auth, utilisateurs, 2FA, profils
  - `bibliotheque/` : fiches, FAQ, recherche
  - `diagnostic/` : quiz, OCR, résultats
  - `accompagnement/` : RDV, messages, tickets
  - `shared/` : sécurité, IA, MinIO, config
- `src/main/resources/` : configurations Spring Boot
- `src/test/` : tests unitaires
- `docker-compose.yml` : services Docker
- `pom.xml` : dépendances Maven

**Frontend :** `activ-education-fronted-main/`
- `activ_education/` : application Flutter
  - `lib/main.dart` : point d'entrée, routes
  - `lib/services/` : API service, base service
  - `lib/screens/` : tous les écrans
  - `lib/models/` : modèles de données
  - `lib/widgets/` : widgets réutilisables
- `backoffice/` : application React
  - `src/main.tsx` : point d'entrée
  - `src/pages/` : pages par rôle
  - `src/components/` : composants
  - `src/stores/` : Zustand stores
- `seed/` : scripts SQL + shell + données universitaires

### Instructions de style

- Police : Times New Roman 14, interligne 1.5, justifié
- Titres : Chapitre = gras 16, Sections = gras 14, Sous-sections = gras 14
- Les extraits de code : Courier New 10, fond grisé
- Tableaux : simples, propres, avec en-têtes
- Figures : minimum 8 (diagrammes UML : cas d'utilisation, classes, séquence(x3), activités, déploiement, MCD, architecture, captures d'écran)
- Tableaux : minimum 3
- Bibliographie : minimum 16 références
- Langue : français, sauf l'abstract en anglais
- Volume attendu : 40-50 pages

### Contenu à produire

Le mémoire doit suivre ce plan (canevas DEFITECH) :

1. **Page de garde** (thème, étudiant, encadreurs, année)
2. **Dédicaces** (1/2 page)
3. **Remerciements** (1-2 pages)
4. **Résumé** (150-250 mots) + **Abstract** anglais
5. **Sommaire** (chapitres niveau 1)
6. **Liste des figures** (min 8)
7. **Liste des tableaux** (min 3)
8. **Liste des sigles et abréviations**
9. **Introduction Générale**
   - Contexte général
   - Problématique
   - Objectifs du rapport
   - Structure du rapport
10. **Chapitre 1 : Présentation de la structure d'accueil et analyse des besoins**
    - HubCity/Woélab : historique, missions, organigramme
    - Analyse de l'existant : système actuel, critique, solutions existantes
    - Besoins fonctionnels (tableau) et non fonctionnels
11. **Chapitre 2 : Conception de la solution**
    - Méthodologie (cycle en V + Agile justifié)
    - UML : cas d'utilisation, séquence (3+), classes, activités, déploiement
    - Base de données : MCD, MLD, dictionnaire de données
    - Interfaces : charte graphique, maquettes
12. **Chapitre 3 : Implémentation, Tests et Résultats**
    - Environnement technique (matériel, logiciels, technologies)
    - Architecture et structure du projet
    - Fonctionnalités clés (auth, quiz, OCR, ORIA)
    - Sécurité implémentée
    - Tests (stratégie, plan, résultats)
    - Présentation application finale (captures)
13. **Chapitre 4 : Discussion, Bilan et Perspectives**
    - Résumé des réalisations (tableau)
    - Points forts et limites
    - Perspectives (7 pistes d'évolution)
    - Bilan personnel du stage
14. **Conclusion Générale**
15. **Bibliographie** (16+ références)
16. **Annexes** (code source, SQL, manuels, glossaire)

### Rendu attendu

Un fichier **Word (.docx)** complet, bien formaté, avec :
- Une mise en page professionnelle
- Des emplacements marqués pour les figures à insérer
- Une table des matières automatique
- Une pagination correcte
- Une bibliographie aux normes APA
- Le glossaire et les annexes
