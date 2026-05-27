package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;

import java.time.LocalDateTime;
import java.util.List;
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

    /** Type d'apprenant. */
    private TypeApprenant typeApprenant;

    /** Indique si le compte est actif. */
    private Boolean actif;

    /** Matières préférées (transmises en JSON array depuis le Front). */
    private List<String> matieresPreferees;

    /** Style d'apprentissage préféré. */
    private String styleApprentissage;

    /** Date de création du compte. */
    private LocalDateTime createdAt;
}
