# Lecture des 7 annuaires statistiques — extraction pour le modèle d'orientation

Sources : PDFs de `donnée/`, textes extraits dans `donnée/annuaires_txt/`.
Date d'analyse : 18/08/2026.

## Fichiers

| Fichier | Nature | Taille texte | Contenu clé |
|---|---|---|---|
| ASN_2024_VR.pdf | INSEED national 2024 | 28 791 l. | Ch. 12 BAC par région/série, Ch. 14 supérieur (effectifs UL/UK par faculté) |
| Annuaire_National_2024_2025 | Ministère 2024-2025 | 12 471 l. | **BAC 2ᵉ partie juin 2024 : présentés/admis par série × 30 préfectures × ordre** |
| Annuaire_National_2023_2024 | Ministère 2023-2024 | 12 759 l. | Idem session juin 2023 |
| ANNUAIRE_STATISTIQUE_2024_MARITIME | Région Maritime | 34 429 l. | BAC I/II par série × préfecture (5 ans) × commune, **BAC technique inscrits/admis/taux** |
| Annuaire_Statistique_2024_Region_Savanes | Région Savanes | 5 511 l. | Effectifs/flux 2019-24, pas d'examens |
| Annuaire_Statistique_2022-2023 | Savanes 2018-2023 | 5 133 l. | Effectifs/flux par préfecture, pas d'examens |
| Annuaire_statistique_Regional_2015-2018 | Savanes 2015-2018 | 7 235 l. | Historique, scolarisation, pas d'examens |

---

## 1. Taux de réussite BAC 2ᵉ partie — TOGO (tous ordres, %)

Source : Ministère (présentés/admis réels) + INSEED (taux recoupés à 0,1 pt).

| Série | 2019-20 | 2020-21 | 2021-22 | 2022-23 | 2023-24 (juin 2024) |
|---|---|---|---|---|---|
| A | 63,3 | 74,1 | 73,7 | 83,1 | **48,3** |
| C | 92,6 | 79,6 | 86,5 | 89,8 | **70,3** |
| D | 58,9 | 61,5 | 79,1 | 76,1 | **42,2** |
| BAC technique (ch.13) | 78 | 83 | 78 | 76,3 | 58,9 |

**⚠️ CHUTE MASSIVE 2023 → 2024** (A −34,8 pts ; D −33,9 pts ; C −19,5 pts). Session juin 2024 historiquement sévère. **Le modèle doit intégrer l'année de session comme variable** — ne jamais moyenner naïvement.

### Juin 2024 — présentés/admis réels (Ministère)
| Série | Présentés | Admis | Taux | M | F |
|---|---|---|---|---|---|
| A | 35 045 | 16 939 | 48,3 % | 49,1 % | 47,5 % |
| C | 1 221 | 858 | 70,3 % | 71,6 % | 67,6 % |
| D | 22 118 | 9 328 | 42,2 % | 42,9 % | 40,9 % |

### Juin 2023 — présentés/admis réels
| Série | Présentés | Admis | Taux |
|---|---|---|---|
| A | 35 457 | 29 461 | 83,1 % |
| C | 965 | 867 | 89,8 % |
| D | 21 448 | 16 325 | 76,1 % |

### Par ordre (public vs privé+communautaire), juin 2024
| Série | Public | Privé+Comm. |
|---|---|---|
| A | 44,9 % (27 392/12 307) | **60,5 %** (7 653/4 632) |
| C | 70,3 % (549/386) | 70,2 % (672/472) |
| D | 35,0 % (14 414/5 052) | **55,5 %** (7 704/4 276) |

→ **Le privé réussit +10 à +20 pts** : variable `ordre` discriminante forte.

---

## 2. BAC 2ᵉ partie par région — 2024 (INSEED 12.31-12.33 + Ministère 3.10)

| Région | A | C | D |
|---|---|---|---|
| Grand Lomé | 58,0 | 72,6 | 51,2 |
| Maritime | 50,4 | 76,9 | 36,2 |
| Plateaux Est | 39,5 | — | — |
| Plateaux Ouest | 45,1 | — | — |
| Centrale | 35,5 | 45,0 | 30,1 |
| Kara | 44,0 | 81,8 | 41,8 |
| Savanes | 51,6 | 61,7 | 38,1 |
| **TOGO** | **48,3** | **70,3** | **42,2** |

(2023 : A 74,3-88,6 % ; C 73,9-100 % ; D 60,7-82,9 % selon région.)

---

## 3. BAC 2ᵉ partie par préfecture — Maritime (5 ans, %)

| Préfecture | A 2023-24 | C 2023-24 | D 2023-24 | A 2022-23 |
|---|---|---|---|---|
| Avé | 31,46 | 50,00 | 22,00 | 79,92 |
| Bas-Mono | 49,25 | 10,00 | 21,05 | 94,36 |
| Lacs | 56,03 | 84,09 | 45,66 | 89,20 |
| Vo | 49,51 | 66,67 | 29,43 | 81,40 |
| Yoto | 40,29 | 92,31 | 31,94 | 87,52 |
| Zio | 55,58 | 91,30 | 40,03 | 82,28 |
| **Ensemble** | **50,38** | **76,92** | **36,14** | **84,65** |

Par commune (2023-24) : Avé 1 : 26,96 % ; Bas-Mono 1 : 40,36 % ; Lacs 1 : 56,26 % ; Vo 1 : 40,94 % ; Yoto 1 : 36,02 % ; Zio 1 : 48,53 % (voir Maritime 11.70-11.81 pour les 18 communes, valeurs A/C/D).

⚠️ **Série C = effectifs minuscules** (96 terminales C en Maritime 2023-24 vs 10 742 A+D) → taux instables, prudence statistique.

---

## 4. BAC technique — Maritime (inscrits/admis/taux, 2023-24)

### Tertiaire
| Série | Inscrits | Admis | Taux |
|---|---|---|---|
| G1 (commerce) | 479 | 314 | 66 % |
| G2 (comptabilité) | 1 947 | 1 169 | 60 % |
| G3 (secrétariat) | 1 852 | 945 | 51 % |

### Industrielle
| Série | Inscrits | Admis | Taux |
|---|---|---|---|
| E | 13 | 13 | 100 % |
| F1 (constr. méca) | 74 | 71 | 96 % |
| F2 (électronique) | 265 | 88 | 33 % |
| F3 (électrotech) | 647 | 426 | 66 % |
| F4 (génie civil) | 472 | 316 | 67 % |
| Ti1 | 19 | 19 | 100 % |

(2021-22 : G1 83 %, G2 80 %, G3 87 %.)

---

## 5. Enseignement supérieur — effectifs 2024 (INSEED 14.7)

| Établissement | Composante | F | M | Total |
|---|---|---|---|---|
| UL | FASEG | 12 459 | 9 133 | 21 592 |
| UL | FLLA | 4 265 | 8 864 | 13 129 |
| UL | FSHS | 3 182 | 8 422 | 11 604 |
| UL | FDS | 3 926 | 6 815 | 10 741 |
| UL | FDD | 2 604 | 5 476 | 8 080 |
| UL | FSS | 1 677 | 843 | 2 520 |
| UL | ESA | 92 | 1 832 | 1 924 |
| UL | TOTAL | 29 082 | 44 233 | 73 315 |
| UK | FLESH | 4 174 | 8 020 | 12 194 |
| UK | FASEG | 1 131 | 1 469 | 2 600 |
| UK | FAST | 474 | 1 587 | 2 061 |
| UK | FDSP | 530 | 865 | 1 395 |
| UK | FSS | 75 | 138 | 213 |
| UK | TOTAL | 6 721 | 12 502 | 19 223 |
| **Total univ. publiques** | | 35 803 | 56 735 | **92 538** |

National 2024 : 113 938 étudiants (dont privés hors univ. 15 348, publics hors univ. 5 395, UCAO-UUT 657).
Par diplôme (2024) : Licence fond. 98 681 · Licence pro 5 674 · Master rech. 3 841 · BTS 1 477 · Master pro 457 · Doctorat 1 334.
Taux d'admissibilité BTS : 46,5 % (2021) → 44,1 % (2024).

---

## 6. Flux secondaires utiles (features contexte)

- Cohorte Terminale TOGO 2024-25 : A 45 134 · C 857 · D 40 429 (2nd cycle total 214 868)
- Ratio admis-BAC2 / effectif Terminale 2024 : A 37,5 % · C ~100 % · D 54,6 %
- Transition 1er → 2nd cycle : 60,0 % (Savanes 2023-24) ; accès en seconde : 23,6 % (2025)
- Taux d'achèvement 2nd cycle : 15,6 % (Savanes) à 33,4 % (Maritime) ; redoublement 2nd cycle 21,6 % ; abandon 9,9 % (Maritime)
- BEPC (juin 2024) par préfecture (Ministère p.119-122) ; CEPD par commune (Maritime 9.47-9.52)

---

## 7. Conclusion — exploitabilité pour le modèle

**Exploitable directement :**
- P(ADMIS BAC | série, ordre, région, préfecture, sexe, année) — sessions 2023 et 2024, 30 préfectures
- BAC technique par filière avec dénominateurs (Maritime)
- Effectifs supérieur par faculté (priors d'offre)
- Terminales par série/région (volume candidats)

**Manquant (les annuaires ne le contiennent pas) :**
- Séries E, F1-F4, G1-G3 au niveau national (technique seulement en Maritime)
- Effectifs par **filière précise** au supérieur (seulement par faculté)
- Concours d'entrée / capacités d'accueil / nouveaux inscrits au supérieur
- Succès/échec au supérieur (labels du modèle)
- Orientation post-bac

**Pièges OCR à corriger si parsing automatique :** colonnes 2020-21 désalignées (12.28/12.30), « Plateaux » scindé Est/Ouest à partir de 2020-21, tableau 11.31 2021-22 (~10× trop grand), en-têtes « Mai 2023 » erronés (session = juin 2023).
