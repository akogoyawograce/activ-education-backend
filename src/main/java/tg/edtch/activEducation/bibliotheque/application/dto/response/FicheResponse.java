package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FicheResponse {

    private UUID trackingId;
    private String titre;
    private String resume;
    private String contenu;
    @Builder.Default
    private Set<String> imageUrls = new HashSet<>();
    @Builder.Default
    private Set<String> videoUrls = new HashSet<>();
    @Builder.Default
    private Set<String> documentUrls = new HashSet<>();

    private Boolean estPublie;
    private Long nbConsultations;
    private String typeFiche;
}
