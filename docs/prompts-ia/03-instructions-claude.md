# Instructions pour Claude — Personnalisation du mémoire Activ Education

## Informations personnelles à utiliser dans le mémoire

**Étudiant** : [Ton Nom Prénoms]
**Institut** : Institut Polytechnique DEFITECH (Togo)
**Licence** : Professionnelle en Génie Logiciel
**Structure d'accueil** : HubCity / Woélab
**Maître de Stage** : [Nom du maître de stage] — [Son titre]
**Directeur du Mémoire** : [Nom du directeur] — [Son titre]
**Année Académique** : 2025-2026
**Ville** : [Ville où tu as fait le stage]

## Consignes pour Claude

1. **Utilise mon nom complet** partout où l'étudiant est mentionné
2. **Écris à la première personne du singulier** ("j'ai réalisé", "mon stage", "mon travail") dans :
   - Les dédicaces
   - Les remerciements
   - Le bilan personnel (section 4.4)
3. **Écris à la troisième personne** dans le reste du mémoire ("l'étudiant", "le stagiaire")
4. **Personnalise les remerciements** avec des vrais noms :
   - Remercie le directeur de HubCity/Woélab
   - Remercie ton maître de stage
   - Remercie ton directeur de mémoire à DEFITECH
   - Remercie ta famille
   - Remercie tes camarades de promotion
5. **Adapte le contenu au contexte togolais** :
   - Parle du système éducatif togolais (collège, lycée, bac, universités de Lomé/Kara/UCAO)
   - Mentionne les défis spécifiques au Togo (manque de conseillers, zones rurales, etc.)
   - Utilise des exemples concrets du terrain togolais
6. **Reste factuel et professionnel** — pas d'exagération, pas de promesses non tenues
7. **Volume** : 40-50 pages, police Times New Roman 14, interligne 1.5, justifié
8. **Langue** : français sauf l'abstract en anglais
9. **Fichier de sortie** : Word (.docx) avec table des matières automatique, numéros de page, figures, tableaux

## Sections à compléter obligatoirement par moi

- **Dédicaces** : je les écris moi-même (très personnel)
- **Remerciements** : je donne les noms, Claude fait la rédaction
- **Bilan personnel** (4.4) : je donne les grandes lignes, Claude rédige
- **Page de garde** : Claude remplit avec mes infos

## Projet résumé pour Claude

J'ai développé la plateforme **Activ Education** pendant mon stage à HubCity/Woélab. C'est une plateforme d'orientation scolaire pour les élèves togolais avec :
- Backend Spring Boot 4.0.5 / Java 21
- Mobile Flutter (élèves)
- Backoffice React 19 / TypeScript 6 (admins)
- PostgreSQL 16 + pgvector
- IA : OpenAI + Groq
- Fonctionnalités : quiz RIASEC, OCR bulletins, assistant ORIA, visio Jitsi, 2FA, tickets
- 28 tests unitaires backend
- Déploiement Docker Compose + Nginx

Lis `prompt_claude_memoire.md` et `memoire_activ_education.md` pour tous les détails.
