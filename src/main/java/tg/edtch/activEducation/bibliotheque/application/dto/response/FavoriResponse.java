package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriResponse {

    private UUID trackingId;
    private UUID utilisateurTrackingId;
    private UUID ficheTrackingId;
    private String ficheTitre;
    private String notePersonnelle;
    private LocalDateTime createdAt;
}
