package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.enums.TypePeriode;

/**
 * DTO d'entrée pour l'upload d'UN bulletin scolaire.
 *
 * <p>Utilisé par {@code BulletinUploadController} :
 * <ul>
 *   <li>Mono : 5 {@code @RequestParam} dans la requête multipart.</li>
 *   <li>Batch : N instances sérialisées en params parallèles
 *       (Spring gère la conversion automatique des tableaux).</li>
 * </ul>
 *
 * <p>Note : {@code MultipartFile} est exclu de la validation
 * {@code @Valid} au niveau controller (le contenu binaire ne se valide
 * pas en JSR-380). La taille et le type MIME sont gérés par
 * {@code application.properties} (20MB max) et par la vérification
 * explicite dans l'orchestrateur.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinUploadRequest {

    /**
     * Fichier du bulletin (PDF ou image JPEG/PNG). Validé côté
     * controller (taille, MIME) — pas de {@code @NotNull} ici car
     * les tests unitaires mockent un fichier.
     */
    private MultipartFile file;

    /**
     * Année scolaire au format "2024-2025" (4 chiffres + tiret + 4 chiffres).
     * Cohérent avec le format utilisé par {@code NoteSaisiManuel.anneeScolaire}.
     */
    @NotBlank(message = "L'année scolaire est obligatoire")
    @Pattern(regexp = "^\\d{4}-\\d{4}$",
             message = "Format attendu : YYYY-YYYY (ex. 2024-2025)")
    private String anneeScolaire;

    /**
     * Période dans l'année (début / milieu / fin).
     */
    @NotNull(message = "La période est obligatoire")
    private Periode periode;

    /**
     * Type de découpage (trimestriel ou semestriel).
     */
    @NotNull(message = "Le type de période est obligatoire")
    private TypePeriode typePeriode;

    /**
     * Numéro de la période (1, 2 ou 3). Pour un système semestriel,
     * seules les valeurs 1 et 2 sont utilisées en pratique.
     */
    @NotNull(message = "Le numéro de période est obligatoire")
    @Min(value = 1, message = "Le numéro de période doit être >= 1")
    @Max(value = 3, message = "Le numéro de période doit être <= 3")
    private Integer numeroPeriode;
}
