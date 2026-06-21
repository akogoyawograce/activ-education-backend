# Backend Changelog

## 2026-05-27

### Feat: ajout champ `niveau` aux fiches établissement

**Contexte :** Les établissements (universités, écoles, lycées) doivent pouvoir
indiquer leur niveau d'études (ex: Bac, Licence, Master, Doctorat, Secondaire).

**Modifications :**

| Fichier | Changement |
|---------|-----------|
| `.../bibliotheque/domain/entite/FicheEtablissement.java` | Ajout colonne `niveau` (VARCHAR 100) |
| `.../bibliotheque/application/dto/request/FicheEtablissementRequest.java` | Ajout champ `niveau` (optionnel) |
| `.../bibliotheque/application/dto/response/FicheEtablissementResponse.java` | Ajout champ `niveau` |
| `.../bibliotheque/application/mapper/FicheEtablissementMapper.java` | Mapping de `niveau` dans toEntity / toResponse / updateFromRequest |
| `.../bibliotheque/repository/FicheEtablissementRepository.java` | Ajout `findByNiveauIgnoreCaseAndEstPublieTrue` |
| `.../bibliotheque/domain/service/FicheEtablissementService.java` | Ajout `listerParNiveau` |
| `.../bibliotheque/domain/service/serviceImple/FicheEtablissementServiceImpl.java` | Implémentation `listerParNiveau` |
| `.../bibliotheque/application/controller/FicheEtablissementController.java` | Ajout `GET /niveau/{niveau}` |

## 2026-05-25

### Fix: validation motDePasse bloquait les mises à jour de profil

**Problème :** `EleveRequest.java` avait `@NotBlank` + `@Size(min = 8)` sur le champ
`motDePasse`. Quand Flutter envoyait un `PUT /api/v1/eleves/{id}` sans mot de passe
(mise à jour partielle du profil), la validation JSR-303 (`@Valid`) rejetait la
requête en 400.

**Modifications :**

| Fichier | Changement |
|---------|-----------|
| `src/main/java/tg/edtch/activEducation/profil/application/dto/request/EleveRequest.java` | Retiré `@NotBlank` et `@Size(min = 8)` de `motDePasse` |
| `src/main/java/tg/edtch/activEducation/profil/domain/service/serviceImple/EleveServiceImpl.java` | Ajouté validation manuelle dans `inscrireEleve()` (null/blank + longueur < 8) |

**Pourquoi ça ne casse pas la création :** la validation est déplacée dans la
méthode `inscrireEleve()` du service — elle vérifie que le mot de passe est
présent et fait au moins 8 caractères avant de créer un compte.
