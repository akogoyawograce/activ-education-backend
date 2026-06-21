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
public class FicheSerieResponse extends FicheResponse {

    private String niveau;
    private String matieresPrincipales;
    private String debouches;
    private String coefficients;

    private Set<FicheResponse> filieresAssociees;
}
