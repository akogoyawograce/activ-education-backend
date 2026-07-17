package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Saisie d'une ligne d'historique de notes pour un élève.
 *
 * <p>Utilisé pour construire la trajectoire académique (3 années consécutives)
 * consommée par le moteur de recommandation Phase 3.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesHistoriqueRequest {

    @NotBlank(message = "L'année scolaire est obligatoire (format YYYY-YYYY)")
    @Size(max = 9, message = "Format attendu : YYYY-YYYY")
    private String anneeScolaire;

    @NotBlank(message = "La classe est obligatoire")
    @Size(max = 50)
    private String classe;

    /**
     * Niveau normalisé (libellé libre côté API pour rétrocompat, parsé via
     * {@code NiveauScolaire.parse()} côté mapper).
     */
    private String niveau;

    /** Libellé de matière, ou {@code null} pour moyenne générale. */
    @Size(max = 100)
    private String matiere;

    @NotNull(message = "La moyenne est obligatoire")
    @DecimalMin(value = "0.00", message = "La moyenne doit être ≥ 0")
    @DecimalMax(value = "20.00", message = "La moyenne doit être ≤ 20")
    private BigDecimal moyenne;

    @Builder.Default
    private Boolean estPartielle = false;

    @Builder.Default
    private Boolean estMoyenneGenerale = false;
}
