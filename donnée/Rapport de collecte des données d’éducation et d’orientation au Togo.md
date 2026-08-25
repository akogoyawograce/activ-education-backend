# Rapport de collecte des données d’éducation et d’orientation au Togo

**Date de collecte : 18 août 2026**

## Résultat de la collecte

J’ai vérifié les pages officielles accessibles et téléchargé les fichiers disponibles auprès de l’INSEED, de la Direction de la planification de l’éducation et de l’Université de Lomé. Les deux annuaires nationaux constituent des sources statistiques et institutionnelles utiles. Les fichiers de l’Université de Lomé récupérés correspondent à des **avis et dossiers de concours**, et non à des listes nominatives d’admis.

| Source | Fichier obtenu | Type | Utilité pour le projet |
|---|---|---|---|
| INSEED | `inseed_annuaire_national_2024.pdf` | PDF, environ 17 Mo | Statistiques nationales, tableaux d’éducation et indicateurs agrégés |
| Ministère / Direction de la planification | `ministere_annuaire_national_2024_2025.pdf` | PDF, environ 10 Mo | Annuaire officiel 2024–2025, établissements et statistiques scolaires |
| Université de Lomé | 14 PDF de concours | PDF | Référentiel des concours, formations, conditions et filières proposées |

## Fichiers de l’Université de Lomé

Les fichiers suivants ont été récupérés depuis la page officielle consacrée aux concours d’entrée en première année : [page source de l’Université de Lomé][3]. Ils décrivent les concours ou formations suivants :

| Fichier | Domaine probable |
|---|---|
| `Concours-EPL.pdf` | École polytechnique de Lomé |
| `Concours-ESAAd.pdf` | École supérieure d’agronomie / domaine associé |
| `Concours-ESTBA.pdf` | Sciences et technologies biologiques et alimentaires |
| `concours-FSHS-Psychologie-Appliquee.pdf` | Psychologie appliquée |
| `Concours-ISICA.pdf` | Information, communication ou arts selon l’intitulé du concours |
| `Concours-IUT-G-LMD.pdf` | Institut universitaire de technologie et formations LMD |
| `EAM.pdf` | École ou formation d’administration et de management |
| `ESA.pdf` | École supérieure d’agronomie |
| `FSS.pdf` | Faculté des sciences de la santé |
| `INJS.pdf` | Institut national de la jeunesse et des sports |
| `INSE-formation-des-enseignants-.pdf` | Formation des enseignants |
| `INSE-SEF.pdf` | Sciences de l’éducation / formation |
| `IUT-G-formation-continue.pdf` | Formation continue de l’IUT de gestion |
| `Capacite-de-Droit.pdf` | Capacité en droit |

## Ce qui manque encore

La page de l’Université de Lomé consultée publie des appels à candidatures et des dossiers de concours, mais elle ne fournit pas, dans cette page, les listes de candidats admis ou recalés nécessaires pour créer directement les labels `ADMIS` et `RECALE`. Ces PDF peuvent néanmoins servir à construire le référentiel des filières, des établissements et des conditions d’accès.

Pour obtenir les labels réels, il faudra poursuivre la recherche sur les pages intitulées **résultats des concours**, **listes des candidats retenus**, **admissions** ou **inscriptions**, ainsi que sur les sites de l’Université de Kara, des écoles privées et de l’Office du Baccalauréat. Les annuaires INSEED et ministériel ne contiennent pas de profils individuels d’élèves : ils doivent être utilisés comme agrégats et référentiels, pas comme dataset d’entraînement individuel.

## Recommandation d’utilisation dans le pipeline ML

| Fichier ou source | Peut alimenter directement `orientation_outcome` ? | Utilisation recommandée |
|---|---:|---|
| Annuaire INSEED 2024 | Non | Calibrer les taux, effectifs et indicateurs par niveau ou région |
| Annuaire national 2024–2025 | Non | Référencer les établissements et vérifier les statistiques officielles |
| PDF de concours de l’Université de Lomé | Non | Construire le catalogue des filières et des conditions d’accès |
| Listes officielles d’admis à rechercher | Oui, après contrôle | Produire les labels par filière et année |
| Profils Activ Education | Oui, après consentement et pseudonymisation | Fournir les variables explicatives : série, notes, RIASEC et engagement |

Il ne faut donc pas injecter directement ces PDF comme 5 000 lignes d’entraînement. La prochaine étape consiste à extraire les filières et conditions d’admission, puis à collecter des listes de résultats nominatives ou agrégées autorisées. Les profils correspondants devront ensuite être saisis ou importés dans Activ Education avant validation par les conseillers.

## Sources vérifiées

[1]: https://inseed.tg/annuaires/ "INSEED – Annuaires statistiques"
[2]: https://planifeducation.gouv.tg/annuaire/ "Direction de la planification – Annuaire national"
[3]: https://univ-lome.tg/appels-a-candidatures-aux-differents-concours-dentree-en-1ere-annee-a-luniversite-de-lome/ "Université de Lomé – Appels à candidatures aux différents concours"
[4]: https://planifeducation.gouv.tg/wp-content/uploads/2025/09/Annuaire_National_2024_2025_29_09_25_2.pdf "Annuaire national de l’éducation 2024–2025"
