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
public class FicheMetierResponse extends FicheResponse {

    private String secteur;
    private String missions;
    private String competences;
    private String formationsAcces;
    private String debouchesTogo;
    private String fourchetteSalaire;

    private Set<FicheResponse> filieresPreparantes;
}
