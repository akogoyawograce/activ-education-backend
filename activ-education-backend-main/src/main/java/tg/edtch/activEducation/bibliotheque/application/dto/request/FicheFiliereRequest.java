package tg.edtch.activEducation.bibliotheque.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheFiliereRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @NotBlank(message = "Le résumé est obligatoire")
    @Size(max = 1000, message = "Le résumé ne peut pas dépasser 1000 caractères")
    private String resume;

    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;

    @NotNull(message = "Le statut de publication est obligatoire")
    private Boolean estPublie;

    @NotBlank(message = "La durée est obligatoire")
    private String duree;

    @NotBlank(message = "Le niveau requis est obligatoire")
    private String niveauRequis;

    private String conditionsAdmission;
    private String programme;
    private String debouchesMetiers;
    private String domaine;

    private Set<UUID> seriesTrackingIds;

    private List<String> niveaux;
}
