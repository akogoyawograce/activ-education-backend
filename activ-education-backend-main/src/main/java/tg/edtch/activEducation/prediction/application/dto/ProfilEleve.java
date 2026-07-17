package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Profil d'un élève agrégé pour le moteur de recommandation.
 *
 * <p>Source : {@code TestRIASECResultat} (6 scores), {@code NotesHistorique}
 * (3 dernières moyennes), {@code Eleve.niveau}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilEleve {

    /** Identifiant interne (Long) de l'élève — pas le trackingId. */
    private Long eleveId;

    /** TrackingId de l'élève (utilisé pour les jointures engagement_signal). */
    private UUID trackingId;

    /** Niveau canonique (peut être null si pas renseigné). */
    private String niveau;

    /** Code RIASEC 3 lettres du dernier test (ex. "RIA"). */
    private String profilDecouvert;

    /** Vecteur 6-dim RIASEC (R, I, A, S, E, C) normalisé 0..1. */
    private List<Double> riasec;

    /** Dernière moyenne annuelle connue. */
    private java.math.BigDecimal noteActuelle;

    /** Note extrapolée pour l'année suivante (issue de la trajectoire). */
    private java.math.BigDecimal noteExtrapolée;

    /** Pente de la trajectoire (positif = progrès, négatif = régression). */
    private java.math.BigDecimal pente;

    /** Confiance 0..1 dans la projection (1.0 si 3 notes, 0.7 si 2, 0.5 si 1). */
    private Double confianceTrajectoire;
}
