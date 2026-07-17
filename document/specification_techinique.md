# SPÉCIFICATIONS TECHNIQUES PAR MODULE — Activ Education

## Module 1 : Authentification et sécurité (shared/security)

### 1.1. JWT (JSON Web Tokens)
- **Bibliothèque** : jjwt 0.12.5
- **Algorithme** : HS512 avec clé 512-bit (64 bytes, base64)
- **Access token** : 15 min (900 000 ms), stocké en flutter_secure_storage
- **Refresh token** : 7 jours (604 800 000 ms), stocké en flutter_secure_storage
- **Filtre** : `JwtAuthenticationFilter` intercepte toutes les requêtes, extrait le Bearer token, vérifie signature + expiration, peuple SecurityContext
- **Refresh flow** : `_refreshWithLock()` dans base_service.dart (Completer lock pour éviter les rafraîchissements concurrents)
- **Entité** : `RefreshToken` lié à l'utilisateur avec date d'expiration
- **Blacklist** : tokens révoqués stockés dans Redis

### 1.2. Connexion / Login
- **Controller** : `AuthController` (`POST /api/v1/auth/login`)
- **Service** : `AuthService` / `AuthServiceImpl`
- **DTO** : `LoginRequest` (email, motDePasse), `TokenResponse` (accessToken, refreshToken, trackingId, nom, typeUtilisateur, requires2fa?, challengeToken?)
- **Rate limiting** : 20 tentatives par fenêtre de 15 minutes par IP (Redis)
- **BCrypt** : coût 12

### 1.3. 2FA / TOTP
- **Entité** : `TotpSecret` (utilisateur_id, secret_base32, active, date_activation)
- **Service** : `TotpService` (RFC 6238, HMAC-SHA1, Base32, période 30s, 6 chiffres)
- **Controller** : `TotpController`
  - `POST /api/v1/auth/2fa/generate` → secret + QR code URL
  - `POST /api/v1/auth/2fa/verify` → active TOTP
  - `POST /api/v1/auth/2fa/disable` → désactive TOTP
  - `POST /api/v1/auth/2fa/validate` → valide challenge (challengeToken + code 6 chiffres)

### 1.4. Mot de passe oublié
- `POST /auth/forgot-password` → envoie OTP par email
- `POST /auth/otp/verify` → vérifie code + retourne resetToken
- `POST /auth/reset-password` → change le mot de passe

### 1.5. Inscription
- `POST /api/v1/eleves` → crée un élève (public)
- `POST /api/v1/parents` → crée un parent (public)
- Conseillers et admins : création par ADMIN uniquement

### 1.6. Contrôle d'accès (@PreAuthorize)
- Bean Spring `@security` avec méthodes :
  - `isOwner(#trackingId)` → propriétaire de la ressource
  - `isOwnChild(#eleveTrackingId)` → parent de l'élève
  - `isOwnConseiller(#conseillerTrackingId)` → conseiller concerné
  - `isRdvParticipant(#rdvTrackingId)` → participant au RDV
- Rate limiting : `RateLimitingFilter` avant `JwtAuthenticationFilter`

---

## Module 2 : Profil utilisateur (profil/)

### 2.1. Hiérarchie des entités
- **Base** : `Utilisateur` (table abstraite) → `Eleve`, `Parent`, `Conseiller`, `Administrateur`
- **Héritage** : `InheritanceType.JOINED`
- **Champs communs** : email, motDePasse (hash BCrypt), nom, prenom, telephone, photoUrl, emailVerified, actif, trackingId (UUID)
- **Relations** : `parent_eleve` (table de liaison), `conseiller_eleve`

### 2.2. Types d'apprenant (enum)
- `AUTRE`, `COLLEGIEN`, `LYCEEN`, `PROFESSIONNEL`, `ECOLIER`, `ETUDIANT`
- Stocké dans `type_apprenant` (TEXT)
- Utilisé pour le routage des dashboards (Flutter) et les règles de validation OCR

### 2.3. Entités profil
| Entité | Attibuts spécifiques |
|--------|---------------------|
| `Eleve` | typeApprenant, dateNaissance, niveauEtude, serie, filiereFavoriteId, etablissementId, matieresPreferees (CSV), centreInteret |
| `Parent` | profession, enfants (List<Eleve>) |
| `Conseiller` | specialite, disponibilites |
| `Administrateur` | niveauAcces (ADMIN, SUPER_ADMIN) |
| `Document` | eleveId, type (BULLETIN, CV, ATTESTATION, AUTRE), minioObjectId, nomFichier, taille |
| `NoteSaisiManuel` | eleveId, matiere, note, coefficient, anneeScolaire |
| `Historique` | eleveId, action, details, date |
| `Notification` | utilisateurId, titre, message, lue, date |
| `ConsentementParental` | eleveId, emailParent, tokenValidation, consenti, ipValidation, dateValidation |

### 2.4. API Profil
| Endpoint | Fonction |
|----------|----------|
| `GET/PUT /api/v1/eleves/{trackingId}` | CRUD élève |
| `GET/PUT /api/v1/parents/{trackingId}` | CRUD parent |
| `GET/PUT /api/v1/conseillers/{trackingId}` | CRUD conseiller |
| `GET/PUT /api/v1/administrateurs/{trackingId}` | CRUD admin |
| `POST /api/v1/eleves/{id}/documents` | Upload document |
| `GET /api/v1/eleves/{id}/documents` | Liste documents |
| `DELETE /api/v1/eleves/{id}/documents/{docId}` | Supprimer document |
| `GET/POST /api/v1/eleves/{id}/notes` | CRUD notes saisie manuelle |
| `GET /api/v1/eleves/{id}/historique` | Historique activités |
| `GET /api/v1/eleves/{id}/notifications` | Notifications |
| `POST /api/v1/parents/{id}/enfants/{eleveId}` | Lier enfant |
| `DELETE /api/v1/parents/{id}/enfants/{eleveId}` | Délier enfant |

### 2.5. Relevé de notes (validation niveau)
- **Controller** : `ReleveNotesController` (`POST /api/v1/eleves/{trackingId}/releve-notes`)
- **Service** : `ReleveNotesService`
- **Multipart** : upload fichier (PDF/image)
- **Méthodes** :
  1. Extraction texte (PDFBox ou OpenAI Vision)
  2. Analyse IA (Groq) avec prompt structuré → JSON décision
  3. Fallback regex (mots-clés ADMIS/BEPC/BAC)
- **Règles métier** :
  - COLLEGIEN + BEPC ADMIS → LYCEEN (Seconde)
  - LYCEEN + BAC ADMIS → ETUDIANT (Licence 1)
  - Type incompatible → rejet

---

## Module 3 : Bibliothèque (bibliotheque/)

### 3.1. Hiérarchie des fiches
- **Base** : `Fiche` (abstract, InheritanceType.JOINED)
- **Tables filles** : `FicheSerie`, `FicheFiliere`, `FicheMetier`, `FicheEtablissement`
- **Champs communs** : titre, resume, contenu, motsCles, ville, estPublie, embedding vector(768)
- **SuperBuilder** Lombok sur les classes concrètes

### 3.2. Entités
| Entité | Attributs spécifiques |
|--------|----------------------|
| `FicheSerie` | code (ex: "S1-SE"), specialite |
| `FicheFiliere` | duree, niveauEntree, debouches, seriesAssociees |
| `FicheMetier` | secteur, competencesRequises, salaireMoyen, filieresDebouchant |
| `FicheEtablissement` | type (PUBLIC/PRIVE), adresse, latitude, longitude, siteWeb, filieresProposees |
| `EntreeFAQ` | question, reponse, categorie, estPublie, nbUtile, nbPasUtile |
| `Favori` | utilisateurId, ficheId |
| `LienInterFiche` | sourceType, sourceTrackingId, targetType, targetTrackingId, typeLien |
| `RechercheOrpheline` | termeRecherche, date, nbResultats |

### 3.3. API Bibliothèque
| Endpoint | Fonction |
|----------|----------|
| `GET /api/v1/bibliotheque/{type}` | Liste paginée par type (etablissement, filiere, metier, serie) |
| `GET /api/v1/bibliotheque/{trackingId}` | Détail d'une fiche |
| `GET /api/v1/bibliotheque/recherche-fiche-ia?q=` | Recherche vectorielle |
| `GET /api/v1/bibliotheque/{trackingId}/liees` | Fiches liées (graphe inter-fiches) |
| `POST/PUT/DELETE /api/v1/bibliotheque/{type}/{id}` | CRUD (admin) |
| `GET /api/v1/bibliotheque/faq` | Liste FAQ publique |
| `GET /api/v1/bibliotheque/faq/{id}` | Détail FAQ |
| `POST /api/v1/bibliotheque/faq/{id}/vote` | Vote Utile/Pas utile |

### 3.4. Recherche vectorielle
- **Provider** : OpenAI `text-embedding-3-small` (768 dimensions)
- **Méthode** : `GET /api/v1/bibliotheque/recherche-fiche-ia?q=texte`
  - Conversion de la requête en embedding via `AIEmbeddingService`
  - Native SQL : `ORDER BY embedding <=> :queryEmbedding LIMIT 20`
  - Récupération des entités via JPQL (contournement JOINED discriminator)
- **Fallback** : `WHERE titre ILIKE %q% OR resume ILIKE %q% OR contenu ILIKE %q% OR ville ILIKE %q%`
- **Pondération** : titre reçoit un poids plus élevé

### 3.5. Graphe inter-fiches
- Entité `LienInterFiche` avec types : DEBOUCHE, PREREQUIS, LOCALISATION, ASSOCIE
- Navigation : depuis une fiche, retourne toutes les fiches liées avec le type de lien
- Utilisé dans les fiches détail (Flutter) pour montrer les relations

---

## Module 4 : Quiz et diagnostic (diagnostic/)

### 4.1. Entités
| Entité | Attributs |
|--------|-----------|
| `Quiz` | titre, description, domaine, estActif, nbQuestions |
| `Question` | texte, type (RIASEC/CONNAISSANCE/INTERET/PERSONNALITE), difficulte, niveauCible, quizId |
| `Reponse` | texte, scores RIASEC (6 colonnes R,I,A,S,E,C), questionId |
| `ResultatDiagnostic` | eleveId, quizId, scores JSON, typeDiagnostic, date |
| `ScoreMatrice` | intitule, colonne, rangee, score |
| `SeuilAdmission` | filiereId, matiere, coefficient, seuilMin, seuilRecommande |
| `QuizIA` | eleveId, ficheTrackingId, etat, questions JSON, dateGeneration |

### 4.2. Quiz adaptatif
- Algorithme : les questions sont réordonnées en fonction des domaines sous-testés
- À chaque réponse, le système calcule le score RIASEC cumulé
- `prochaineQuestionTrackingId` : la question suivante priorise le domaine le moins exploré
- Les questions sont chargées en mémoire pour la session (évite N+1 queries)

### 4.3. Scoring
- Score final = (poids_quiz × score_quiz) + (poids_academique × score_academique)
- Poids configurables via `ParametreApplication` (entité + controller)

### 4.4. Génération de quiz par IA
- **Controller** : `QuizGenerationController` (`POST /api/v1/quiz/generate`)
- **Service** : `QuizGenerationService` / `QuizGenerationServiceImpl`
- **Prompt** : envoie la description de la fiche à GPT-4o-mini qui génère des questions/réponses avec scores RIASEC
- **Stockage** : entité `QuizIA` pour sauvegarder l'état de la génération

### 4.5. Seuils d'admission
- `SeuilAdmissionService` : normalise les accents et tirets pour matcher les matières
- Calcul du score pondéré par filière candidate
- Matching utilisé dans l'analyse de notes

### 4.6. API Diagnostic
| Endpoint | Fonction |
|----------|----------|
| `GET /api/v1/quiz` | Liste quiz |
| `GET /api/v1/quiz/{id}` | Détail quiz + questions |
| `POST /api/v1/quiz` | Création quiz (admin) |
| `GET /api/v1/questions/{id}/reponses` | Réponses d'une question |
| `POST /api/v1/resultats-diagnostic` | Soumettre résultat quiz |
| `GET /api/v1/eleves/{id}/resultats-diagnostic` | Résultats d'un élève (Page) |
| `GET /api/v1/seuils-admission` | Liste seuils |
| `POST /api/v1/quiz/generate` | Génération IA de quiz |

---

## Module 5 : Accompagnement (accompagnement/)

### 5.1. Messagerie
- **Entité** : `Message` (expediteur, destinataire, contenu, lu, dateEnvoi)
- **Pas de WebSocket** : polling Flutter toutes les 4 secondes
- **Controller** : `MessageController`

### 5.2. Tickets
- **Entité** : `Ticket` (eleveId, conseillerId, sujet, statut, dateCreation)
- **Statuts** : OUVERT → ASSIGNE → EN_COURS → RESOLU → FERME
- **Assignation** : round-robin automatique parmi les conseillers
- **Controller** : `TicketController`

### 5.3. Rendez-vous
- **Entité** : `RendezVous` (eleveId, conseillerId, dateDebut, dateFin, statut, lienVisio, motif)
- **Visioconférence** : lien Jitsi Meet généré automatiquement (`VisioService`)
- **Disponibilités** : entité `Disponibilite` (conseillerId, jourSemaine, heureDebut, heureFin)
- **Rappels SMS** : CRON job quotidien à 8h00 via `RendezVousReminderService` + `SmsService`

### 5.4. Notifications push
- **Service** : `NotificationPushService`
- **FCM** : Firebase Cloud Messaging
- **Fallback** : silencieux si FCM_SERVER_KEY non configuré
- Sauvegardées en table `notifications`

### 5.5. API Accompagnement
| Endpoint | Fonction |
|----------|----------|
| `GET /api/v1/messages/conversation?autre={id}` | Conversation |
| `POST /api/v1/messages` | Envoyer message |
| `PATCH /api/v1/messages/conversation/lire` | Marquer comme lu |
| `GET /api/v1/rendez-vous` | Liste RDV |
| `POST /api/v1/rendez-vous` | Planifier RDV |
| `PATCH /api/v1/rendez-vous/{id}/annuler` | Annuler RDV |
| `GET /api/v1/tickets` | Liste tickets |
| `POST /api/v1/tickets` | Créer ticket |
| `PATCH /api/v1/tickets/{id}/statut` | Changer statut |

---

## Module 6 : IA et traitement intelligent (shared/ai)

### 6.1. Embeddings
- **Interface** : `AIEmbeddingService`
- **Implémentation** : `OpenAIEmbeddingServiceImpl`
- **Modèle** : `text-embedding-3-small` (768 dimensions)
- **Fallback** : si quota OpenAI épuisé (429), retourne null → le service appelant utilise LIKE

### 6.2. Assistant ORIA
- **Controller** : `OriaController`
  - `POST /api/v1/oria/chat` → envoyer message, recevoir réponse
  - `GET /api/v1/oria/session/{sessionId}` → historique session
  - `DELETE /api/v1/oria/session/{sessionId}` → supprimer session
- **Service** : `OriaService`
  - **Modèle principal** : GPT-4o-mini (OpenAI)
  - **Fallback 1** : Groq (Llama 3.1-8B)
  - **Fallback 2** : Ollama local (qwen2:0.5b)
  - **RAG** : `rechercherContexte()` interroge pgvector + LIKE sur les fiches avant chaque réponse
  - **Prompt système** : inclut le système éducatif togolais, universités, consignes de réponse
- **Entité** : `OriaMessage` (sessionId, role, contenu, date)
- **Repository** : `OriaMessageRepository`

### 6.3. Assistant vocal
- **Controller** : `VocalController`
  - `POST /api/v1/vocal/transcrire` → STT (Whisper API)
  - `POST /api/v1/vocal/chat` → ORIA vocal
  - `POST /api/v1/vocal/synthese` → TTS (OpenAI TTS)
- **Service** : `VocalService`
- **Flutter** : speech_to_text → envoi API → flutter_tts

### 6.4. OCR
- **Controller** : `OcrController` (`POST /api/v1/eleves/{trackingId}/ocr`)
- **Service** : `OcrService`
- **Traitement** :
  1. PDF → PDFBox 3 (extraction texte)
  2. Image → base64 → OpenAI Vision (gpt-4o-mini)
  3. Parser regex → `[{matière, note, coefficient}]`
- **Post-traitement** : correspondance avec `SeuilAdmissionService`

### 6.5. Recommandation IA
- **Interface** : `RecommandationIAService`
- **Analyse** : niveau, filière, métier, notes, quiz, badges
- **Si profil vide** : message guidant vers la complétion du profil
- **Controller** : `RecommandationIAController`

---

## Module 7 : Backoffice (React 19/TypeScript 6)

### 7.1. Architecture
- **Routing** : React Router v7 avec routes protégées (ProtectedRoute)
- **Auth** : Zustand store (`authStore.ts`) avec persistance localStorage
- **Requêtes** : Axios + TanStack Query v5 (staleTime: 5min, gcTime: 10min)
- **Styling** : Tailwind CSS v4 avec thème personnalisé (couleurs primaire #3730E8)
- **Composants UI** : DataTable, Modal, RichTextEditor (contentEditable + execCommand), StatCard, StatusBadge, Skeleton

### 7.2. Niveaux d'accès
| Niveau | Accès |
|--------|-------|
| CONSEILLER | Dashboard, messages, rendez-vous, utilisateurs, ORIA, FAQ, stats |
| ADMIN | Tout conseiller + CRUD fiches/quiz/FAQ/seuils, gestion utilisateurs, statistiques, notifications |
| SUPER_ADMIN | Tout admin + paramètres applicatifs, logs d'audit |

### 7.3. Pages backoffice (29)
- **Login** : LoginPage avec support 2FA/TOTP
- **Conseiller** (8) : Dashboard, Messages, RendezVous, Utilisateurs, Oria, FAQ, Profil, Statistiques
- **Admin** (17) : Dashboard, Eleves, Parents, Conseillers, Filieres, Metiers, Series, Etablissements, Quiz, QuizEditor, FAQ, Seuils, Matrices, Stats, Profil, Notifications
- **Super Admin** (3) : Dashboard, Paramètres, Logs

### 7.4. Éditeur WYSIWYG
- Composant `RichTextEditor` utilisant `contentEditable` + `document.execCommand`
- Barre d'outils : gras, italique, souligné, listes (ordonnées/non), titres (H1-H3), liens
- Non encore intégré aux formulaires

### 7.5. API Services (14 fichiers)
| Service | Endpoints |
|---------|-----------|
| auth.ts | Login, 2FA, refresh, me |
| eleves.ts | CRUD élèves + notes |
| parents.ts | CRUD parents + lien enfants |
| conseillers.ts | CRUD conseillers |
| administrateurs.ts | CRUD admins |
| bibliotheque.ts | CRUD 4 types fiches + FAQ |
| quiz.ts | CRUD quiz + questions + réponses |
| messages.ts | Messages, conversations |
| rendezvous.ts | RDV CRUD |
| notifications.ts | Notifications CRUD |
| stats.ts | KPIs, graphiques |
| logs.ts | Logs d'audit |
| seuils.ts | Seuils admission |
| scoreMatrices.ts | Matrices de score |

---

## Module 8 : Modules fonctionnels avancés

### 8.1. Portfolio de compétences
- **Entité** : `PortfolioCompetence` (eleveId, categorie, intitule, niveau, description)
- **Service** : `PortfolioService` avec `analysePortfolio()` → recommandation métiers basée sur compétences
- **Flutter** : `PortfolioScreen` avec visualisation radar
- **API** : `GET/POST/PUT/DELETE /api/v1/portfolio`

### 8.2. Simulateur de parcours
- **Entité** : `SimulationParcours` (eleveId, scenario JSON, resultats JSON)
- **Service** : `SimulateurParcoursService` : moteur de simulation "what-if"
- **Flutter** : `SimulateurParcoursScreen` + `SimulateurResultatScreen`
- **API** : `POST /api/v1/simulateur` (authentifié)

### 8.3. DataHub (carte thermique)
- **Entité** : `MetierRegionData` (region, metierId, nombreExplorations)
- **Service** : `DataHubService` : agrégation des données d'exploration par région
- **Flutter** : `DataHubScreen` avec heatmap Togo
- **API** : `GET /api/v1/datahub/stats`

### 8.4. Réseau social
- **Entités** : `PublicationReseau`, `CommentaireReseau`, `AbonnementReseau`
- **Service** : `ReseauService` : fil d'actualité, CRUD publications, commentaires, abonnements
- **Flutter** : `ReseauScreen` avec timeline
- **API** : `GET/POST /api/v1/reseau/publications`, `POST /api/v1/reseau/publications/{id}/commentaires`

### 8.5. Badges et défis
- **Entités** : `Badge`, `BadgeDecerne`, `DefiOrientation`, `DefiReleve`
- **Service** : `BadgeService` : attribution automatique basée sur critères (quiz, exploration, profil, entretiens, activité)
- **Flutter** : `BadgeScreen` avec liste et critères (AlertDialog info)
- **API** : `GET /api/v1/badges`, `POST /api/v1/badges/verify`

### 8.6. Simulation d'entretien IA
- **Entité** : `SimulationEntretien` (eleveId, etat, echanges JSON, score, date)
- **Service** : `EntretienService` avec Groq API (prompt structuré, évaluation)
- **Flutter** : `EntretienScreen` (interface chat simulateur)
- **API** : `POST /api/v1/entretien/start`, `POST /api/v1/entretien/{id}/repondre`

### 8.7. Témoignages
- **Entité** : `Temoignage` (eleveId, metierId, contenu, note, estVedette, estPublie)
- **Flutter** : `TemoignageScreen` avec aide contextuelle (IconButton help_outline)
- **API** : `GET /api/v1/temoignages`

### 8.8. Alumni et mentorat
- **Entités** : `Alumni`, `Mentorat`
- **Service** : `AlumniService`, `MentoratService`
- **API** : CRUD alumni, demandes de mentorat

### 8.9. Autres modules
| Module | Entité(s) | Service | Fonction |
|--------|-----------|---------|----------|
| Attestations | `Attestation` | `AttestationService` | Génération certificats |
| Cahier de bord | `EntreeJournal` | `CahierBordService` | Journal orientation |
| Calendrier | `EvenementOrientation` | `CalendrierService` | Événements orientation |
| Carte métiers | `MetierRegionData` | `CarteMetiersService` | Visualisation régionale |
| CV Genere | `CVGenere` | `CVGenereService` | Génération CV |
| Emploi | `OffreEmploi`, `Candidature` | `EmploiService` | Offres et candidatures |
| Hors ligne | `SyncLog` | `HorsLigneService` | Sync différée |
| Parrainage | `Parrainage` | `ParrainageService` | Parrainage élèves |
| Prédiction | `PredictionReussite` | `PredictionService` | Prédiction réussite |
| Réorientation | `DemandeReorientation` | `ReorientationService` | Changement parcours |
| RIASEC | `TestRIASECResultat` | `RIASECService` | Test RIASEC dédié |
| Salle virtuelle | `VisiteVirtuelle` | `VisiteVirtuelleService` | Visites virtuelles |
| VAE | `DossierVAE` | `VAEService` | Validation acquis |

---

## Module 9 : Versioning et audit (shared/util)

### 9.1. Versioning (paper trail)
- **Entité** : `VersionHistorique` (itemType, itemTrackingId, event, whodunnit, objectData JSON, objectChanges JSON, date)
- **Service** : `VersioningService`
- **Méthodes** : `enregistrerCreation()`, `enregistrerModification()`, `enregistrerSuppression()`
- **Diff automatique** : comparaison JSON entre ancien et nouvel état

### 9.2. Audit logs
- **Entité** : `AuditLog` (email, action, ressource, details, ipAddress, userAgent, date)
- **Filtre automatique** : intercepte toutes les requêtes POST/PUT/DELETE
- **Controller** : `AdminLogsController`
- **API** : `GET /api/v1/admin/logs` (filtres : email, action, dates)

### 9.3. Paramètres application
- **Entité** : `ParametreApplication` (cle, valeur, description)
- **Controller** : `ParametreController`
- **Exemples** : NB_QUESTIONS_QUIZ, POIDS_QUIZ, POIDS_ACADEMIQUE

---

## Module 10 : Mode maintenance

### 10.1. Implémentation
- **Filtre** : `MaintenanceFilter` (Servlet Filter, @Order(1))
- **Flag** : statique en mémoire (`MaintenanceFilter.maintenanceMode`)
- **Bypass** : IP privées (127.0.0.1, 10.\*, 172.16.\*, 192.168.\*)
- **Réponse** : 503 avec JSON `{ message: "Maintenance en cours" }`

### 10.2. API
| Endpoint | Fonction |
|----------|----------|
| `GET /api/v1/admin/maintenance` | Voir état maintenance |
| `POST /api/v1/admin/maintenance` | Activer/désactiver (body: { enable: boolean }) |

---

## Module 11 : Infrastructure et monitoring

### 11.1. Docker
- `docker-compose.yml` : db, minio, redis, (optionnel: app)
- `docker-compose.prod.yml` : nginx + app + db + minio + redis
- `docker-compose.monitoring.yml` : prometheus, grafana, elasticsearch, logstash, kibana
- `Dockerfile.prod` : multi-stage build (Maven + JRE)

### 11.2. Monitoring
- Actuator endpoints : `/actuator/health`, `/actuator/prometheus`
- Micrometer + Prometheus registry
- Grafana dashboards pour métriques Spring Boot + PostgreSQL + Redis

### 11.3. Fichiers de déploiement
| Fichier | Rôle |
|---------|------|
| `Dockerfile.prod` | Build multi-stage |
| `docker-compose.prod.yml` | Stack production |
| `nginx.conf` | Reverse proxy, SSL, CSP, HSTS |
| `nginx-local.conf` | Configuration locale sans SSL |
| `.env.prod.example` | Template variables d'environnement |
| `DEPLOY.md` | Instructions déploiement |

---

## Module 12 : Application Flutter (mobile)

### 12.1. Architecture screens (55 écrans)
- **Auth** (11) : Splash, Onboarding, Login, Register, Preferences, ProfileSetup, ForgotPassword, OTP, ResetPassword, TotpSetup, TotpVerify
- **Home/Dashboard** (11) : MainScaffold, DashboardBachelier, DashboardReconversion, DashboardDecrocheur, DashboardParent, DashboardConseiller, RecommandationIA, EnfantSuivi, DiagnosticEnfant, Notifications, FAQ, Support
- **Explorer** (6) : Explorer, CategoryList, FicheDetail, Favorites, EtablissementsMap, CatalogueFilter
- **Search** (1) : GlobalSearch
- **Diagnostic** (4) : Quiz, Resultats, Notes, OcrBulletin
- **Messages** (5) : MessagesList, Chat, Rdv, RdvList, Ticket
- **Modules** (9) : Oria, Simulateur, SimulateurResultat, Portfolio, Datahub, Entretien, Reseau, Badge, Temoignage
- **Others** (5) : Profile, Historique, Documents, Conseillers, Errors(x3)

### 12.2. Services Flutter (19)
- `BaseService` : classe abstraite avec Dio, intercepteur JWT, refresh lock, cache GET 5min
- `ApiService` : facade singleton
- `AuthService`, `AcademicService`, `ExplorerService`, `DiagnosticService`, `InteractionService`
- `FileService`, `ScoreMatriceService`, `ParentService`, `AdminService`
- `PortfolioService`, `DataHubService`, `EntretienService`, `ReseauService`
- `BadgeService`, `TemoignageService`, `RecommendationService` (offline), `VoiceService`

### 12.3. Gestion d'état
- **Pattern** : setState + singletons statiques
- **Pas de Provider/Riverpod/Bloc**
- **Stockage tokens** : flutter_secure_storage → SharedPreferences (web) → mémoire

### 12.4. Routage
- 42 routes nommées dans `main.dart`
- RouteObserver pour analytics
- Arguments passés via `ModalRoute.of(context)?.settings.arguments` (Map)

---

## Module 13 : CSS / Thème (Backoffice)

### 13.1. Tailwind CSS v4
- `@import "tailwindcss"` + `@theme` directive
- Couleurs : primary (#3730E8), secondary (#F59E0B), success (#10B981), danger (#EF4444)
- Sidebar : 240px de large
- Font : Inter, system-ui

### 13.2. Thème Flutter
- `AppTheme` : `AppColors`, `AppTextStyles` (Inter + Poppins)
- `ThemeData.lightTheme()` unique
- Schéma de couleurs cohérent avec le backoffice
