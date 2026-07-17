package tg.edtch.activEducation.temoignage.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemoignageResponse(
    UUID trackingId,
    String auteurNom,
    String auteurPhotoUrl,
    String auteurTitre,
    String filiereSuivie,
    String etablissement,
    String anneeParcours,
    String contenu,
    String videoUrl,
    String metierTrackingId,
    String metierNom,
    String filiereTrackingId,
    boolean estPublie,
    boolean estEnVedette,
    int nbVues,
    LocalDateTime createdAt
) {}
