package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FicheResponse {

    private UUID trackingId;
    private String titre;
    private String resume;
    private String imageUrl;
    private String videoUrl;
    private Boolean estPublie;
    private Long nbConsultations;
    private String typeFiche; // To distinguish between Etablissement, Filiere, etc.
}
