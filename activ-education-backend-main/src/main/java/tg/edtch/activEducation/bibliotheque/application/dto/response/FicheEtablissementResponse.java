package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FicheEtablissementResponse extends FicheResponse {

    private String adresse;
    private String ville;

    private String typeEtablissement;
    private String niveau;
    private String contacts;
    private String siteWeb;
    private String offreFormation;
    private Boolean estPublic;
    private Double latitude;
    private Double longitude;

    private Set<FicheResponse> filieresProposees;
}
