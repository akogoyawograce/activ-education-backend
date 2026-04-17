package tg.edtch.activEducation.bibliotheque.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheMetierRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @NotBlank(message = "Le résumé est obligatoire")
    @Size(max = 1000, message = "Le résumé ne peut pas dépasser 1000 caractères")
    private String resume;

    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;

    private String videoUrl;
    private String imageUrl;

    @NotNull(message = "Le statut de publication est obligatoire")
    private Boolean estPublie;

    @NotBlank(message = "Le secteur est obligatoire")
    private String secteur;

    private String missions;
    private String competences;
    private String formationsAcces;
    private String debouchesTogo;
    private String fourchetteSalaire;
}
