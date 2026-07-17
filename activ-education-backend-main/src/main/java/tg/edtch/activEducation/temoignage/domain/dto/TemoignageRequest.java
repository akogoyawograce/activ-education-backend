package tg.edtch.activEducation.temoignage.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TemoignageRequest(
    @NotBlank @Size(max = 100) String auteurNom,
    String auteurPhotoUrl,
    @Size(max = 200) String auteurTitre,
    @Size(max = 200) String filiereSuivie,
    @Size(max = 200) String etablissement,
    @Size(max = 20) String anneeParcours,
    @NotBlank @Size(max = 5000) String contenu,
    String videoUrl,
    String metierTrackingId,
    String metierNom,
    String filiereTrackingId,
    Boolean estPublie,
    Boolean estEnVedette
) {}
