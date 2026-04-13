package tg.edtch.activEducation.accompagnement.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requête pour l'envoi d'un message.
 * L'expéditeur est identifié via son trackingId dans l'URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotBlank(message = "Le contenu du message est obligatoire")
    private String contenu;

    /** trackingId public du destinataire. */
    @NotNull(message = "Le destinataire est obligatoire")
    private UUID destinataireTrackingId;
}
