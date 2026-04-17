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
public class EntreeFAQRequest {

    @NotBlank(message = "La question est obligatoire")
    @Size(max = 500, message = "La question ne peut pas dépasser 500 caractères")
    private String question;

    @NotBlank(message = "La réponse est obligatoire")
    private String reponse;

    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String categorie;

    @NotNull(message = "Le statut de publication est obligatoire")
    private Boolean estPublie;
}
