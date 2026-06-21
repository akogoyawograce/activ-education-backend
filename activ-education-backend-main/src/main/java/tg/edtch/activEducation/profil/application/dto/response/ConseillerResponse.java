package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un Conseiller.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConseillerResponse {

    /** Identifiant public — seul identifiant exposé à l'extérieur. */
    private UUID trackingId;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    /** Spécialités du conseiller. */
    private String specialites;

    /** Biographie professionnelle. */
    private String biographie;

    /** Qualifications / diplômes. */
    private String qualifications;

    /** Années d'expérience. */
    private Integer anneesExperience;

    /** Charge de travail actuelle (nombre de dossiers actifs). */
    private Integer chargeTravail;

    /** Indique si le compte est actif. */
    private Boolean actif;

    /** Date de création du compte (audit). */
    private LocalDateTime createdAt;
}
