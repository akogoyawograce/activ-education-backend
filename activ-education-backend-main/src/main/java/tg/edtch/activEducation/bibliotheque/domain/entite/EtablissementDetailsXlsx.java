package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Détails étendus (JSON) pour une fiche établissement, alimentés depuis
 * Base_Etablissements_Superieurs_Togo.xlsx.
 *
 * <p>
 * MVP : toutes les feuilles du xlsx (Contacts, Responsables, Localisation,
 * Admissions, Frais, Statistiques, Formations, …) sont sérialisées en JSON
 * dans {@link #donneesEtenduesJson}. Cela évite la création de 7 entités JPA
 * distinctes et reste compatible avec {@code ddl-auto=update} (cf. CLAUDE.md
 * §pièges critiques).
 *
 * <p>
 * Si le besoin de requêter un champ précis (ex: « toutes les écoles dont le
 * téléphone commence par +228 22 ») émerge, on pourra migrer vers des entités
 * typées (sprint dédié).
 *
 * <p>
 * Relation 1-1 avec {@link FicheEtablissement}, contrainte unique sur
 * {@code fiche_id}, générée par Hibernate au démarrage (table
 * {@code etablissement_details_xlsx}).
 *
 * @see <a href="https://github.com/missions-Tog/ActivEducation/issues">Phase 2
 *      du sprint xlsx — 6 août 2026</a>
 */
@Entity
@Table(name = "etablissement_details_xlsx", indexes = {
        @Index(name = "idx_etab_details_fiche", columnList = "fiche_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EtablissementDetailsXlsx extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Identifiant source xlsx (ex : "TG-UL-001"). Nullable si la fiche n'a
     * pas été importée depuis le xlsx.
     */
    @Column(name = "id_source_xlsx", length = 50)
    private String idSourceXlsx;

    /**
     * Référence vers {@code fiches_etablissement.id} — partagée avec la PK
     * de la fiche parente pour rester cohérent avec le pattern JOINED déjà
     * utilisé par {@link FicheEtablissement}.
     */
    @Column(name = "fiche_id", nullable = false, unique = true, updatable = false)
    private Long ficheId;

    /**
     * Toutes les données structurées du xlsx sérialisées en JSON.
     * Exemple de structure :
     * <pre>
     * {
     *   "localisation": { "region": "...", "ville": "...", "latitude": ..., "longitude": ... },
     *   "contacts": { "telephonePrincipal": "...", "email": "...", "siteWeb": "..." },
     *   "responsables": { "recteur": "...", "fondateur": "..." },
     *   "admissions": { "niveauRequis": "...", "concours": true, "seriesBacAcceptees": [...] },
     *   "frais": { "fraisInscription": "...", "boursesInternationales": true },
     *   "statistiques": { "nombreEtudiants": "≈ 25000", "tauxReussite": "67 %" },
     *   "formations": [ { "idFormation": "TG-UL-001-F1", "domaine": "...", ... }, ... ],
     *   "informationsComplementaires": {
     *     "historique": "...", "presentation": "...", "mission": "...",
     *     "devise": "...", "slogan": "...", "anneeCreation": 1970
     *   },
     *   "sources": [ { "url": "...", "dateConsultation": "2026-08-06" }, ... ]
     * }
     * </pre>
     */
    @Lob
    @Column(name = "donnees_etendues_json", columnDefinition = "TEXT")
    private String donneesEtenduesJson;

    /**
     * Date d'extraction depuis le xlsx (pour traçabilité).
     */
    @Column(name = "date_extraction")
    private java.time.LocalDateTime dateExtraction;
}
