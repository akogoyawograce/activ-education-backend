package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse pour un Parent.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 * Les enfants sont exposés sous forme de liste de leurs {@code trackingId}
 * publics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentResponse {

    /** Identifiant public — seul identifiant exposé à l'extérieur. */
    private UUID trackingId;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    /**
     * Liste des trackingId publics des élèves-enfants rattachés.
     * Aucun Long id de base de données n'est exposé ici.
     */
    private List<UUID> enfantsTrackingIds;

    /** Indique si le compte est actif. */
    private Boolean actif;

    /** Date de création du compte (audit). */
    private LocalDateTime createdAt;
}
