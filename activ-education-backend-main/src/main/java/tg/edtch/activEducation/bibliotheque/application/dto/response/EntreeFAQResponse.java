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
public class EntreeFAQResponse {

    private UUID trackingId;
    private String question;
    private String reponse;
    private String categorie;
    private Boolean estPublie;
    private Long nbVues;
    private Long nbUtile;
    private Long nbPasUtile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
