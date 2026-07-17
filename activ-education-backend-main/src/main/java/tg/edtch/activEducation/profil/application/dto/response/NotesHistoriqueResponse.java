package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représentation externe d'une ligne d'historique de notes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesHistoriqueResponse {

    private UUID trackingId;

    private Long eleveId;

    private String anneeScolaire;

    private String classe;

    /** Libellé canonique du niveau (ex. "LYCEE_TLE"). */
    private String niveau;

    /** Libellé humain (ex. "Lycée - Terminale") pour affichage direct. */
    private String niveauLabel;

    private String matiere;

    private BigDecimal moyenne;

    private Boolean estPartielle;

    private Boolean estMoyenneGenerale;

    /** SAISIE_MANUELLE / OCR / IMPORT_CSV. */
    private String source;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
