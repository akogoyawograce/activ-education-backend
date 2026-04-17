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
public class FicheFiliereResponse extends FicheResponse {

    private String duree;
    private String niveauRequis;
    private String conditionsAdmission;
    private String programme;
    private String debouchesMetiers;
    private String domaine;

    private Set<FicheResponse> metiersPrepares;
    private Set<FicheResponse> etablissements;
}
