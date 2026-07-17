package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleveValidationResponse {

    private UUID trackingId;
    private boolean valide;
    private String typeDocument;
    private String candidat;
    private String numeroCandidat;
    private String centre;
    private String serie;
    private double moyenne;
    private String decision;
    private String mention;
    private String niveauAttribue;
    private String typeApprenantAttribue;
    private String message;
    private String raisonRejet;
}
