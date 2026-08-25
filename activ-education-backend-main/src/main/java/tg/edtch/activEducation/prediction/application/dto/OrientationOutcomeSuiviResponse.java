package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vue de suivi conseiller d'un outcome : enrichie du nom de l'élève et du
 * titre de la filière pour l'écran backoffice (aucune donnée sensible en
 * plus du reste).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientationOutcomeSuiviResponse {

    private UUID trackingId;

    private UUID eleveTrackingId;

    private String eleveNom;

    private String elevePrenom;

    private String filiereTitre;

    private LocalDate dateChoix;

    private String serie;

    private String region;

    private String ordre;

    private String sexe;

    private Integer anneeSession;

    private BigDecimal scoreRecommandation;

    /** EN_COURS / ADMIS / RECALE / ABANDON / REORIENTE. */
    private String statut;

    private Integer satisfaction;

    private LocalDate dateMajStatut;

    private String commentaire;

    private LocalDateTime createdAt;
}