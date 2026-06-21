package tg.edtch.activEducation.bibliotheque.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriRequest {

    @NotNull(message = "Le trackingId de l'utilisateur est obligatoire")
    private UUID utilisateurTrackingId;

    @NotNull(message = "Le trackingId de la fiche est obligatoire")
    private UUID ficheTrackingId;

    @Size(max = 255, message = "La note personnelle ne peut pas dépasser 255 caractères")
    private String notePersonnelle;
}
