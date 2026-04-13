package tg.edtch.activEducation.accompagnement.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO de requête pour la création ou modification d'un créneau de
 * disponibilité.
 * Le conseiller est identifié via son trackingId dans l'URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibiliteRequest {

    /**
     * Jour de la semaine ISO : 1 = Lundi, 2 = Mardi, ..., 7 = Dimanche.
     */
    @NotNull(message = "Le jour de la semaine est obligatoire")
    @Min(value = 1, message = "Le jour minimum est 1 (Lundi)")
    @Max(value = 7, message = "Le jour maximum est 7 (Dimanche)")
    private Integer jourSemaine;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;
}
