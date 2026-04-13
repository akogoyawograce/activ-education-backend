package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un Élève.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleveResponse {

    /** Identifiant public — seul identifiant exposé à l'extérieur. */
    private UUID trackingId;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    /** Niveau scolaire : ex. "Terminale C", "Licence 2". */
    private String niveauEtude;

    /** Établissement actuel. */
    private String etablissementActuel;

    /** Filière actuelle. */
    private String filiere;

    /** Année de fin de cycle prévisionnelle. */
    private LocalDate anneeObtentionPrevue;

    /** Indique si le compte est actif. */
    private Boolean actif;

    /** Date de création du compte. */
    private LocalDateTime createdAt;
}
