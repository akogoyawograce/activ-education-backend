package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une entrée d'historique.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 * L'utilisateur est référencé par son trackingId public.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueResponse {

    /** Identifiant public de cette entrée d'historique. */
    private UUID trackingId;

    /** Type d'action enregistrée. */
    private String action;

    /** Détails ou métadonnées de l'action. */
    private String details;

    /** trackingId public de l'utilisateur concerné — jamais son Long id. */
    private UUID utilisateurTrackingId;

    /** Date et heure de l'action (audit automatique via BaseEntity). */
    private LocalDateTime createdAt;
}
