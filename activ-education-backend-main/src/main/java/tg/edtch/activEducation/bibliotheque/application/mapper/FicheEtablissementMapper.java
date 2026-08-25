package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EtablissementDetailsSupplementaires;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;

import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class FicheEtablissementMapper {

    public FicheEtablissement toEntity(FicheEtablissementRequest request, Set<FicheFiliere> filieres) {
        if (request == null)
            return null;
        return FicheEtablissement.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .resume(request.getResume())
                .contenu(request.getContenu())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .typeEtablissement(parseTypeEtablissement(
                        request.getTypeEtablissement() != null ? request.getTypeEtablissement() : "UNIVERSITE"))
                .niveau(request.getNiveau())
                .contacts(request.getContacts())
                .siteWeb(request.getSiteWeb())
                .offreFormation(request.getOffreFormation())
                .estPublic(request.getEstPublic() != null ? request.getEstPublic() : true)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .filieresProposees(new java.util.HashSet<>(filieres))
                .build();
    }

    public FicheEtablissementResponse toResponse(FicheEtablissement entity) {
        if (entity == null)
            return null;
        return FicheEtablissementResponse.builder()
                .trackingId(entity.getTrackingId())
                .titre(entity.getTitre())
                .resume(entity.getResume())
                .contenu(entity.getContenu())
                .imageUrls(entity.getImageUrls() != null ? new java.util.HashSet<>(entity.getImageUrls())
                        : new java.util.HashSet<>())
                .videoUrls(entity.getVideoUrls() != null ? new java.util.HashSet<>(entity.getVideoUrls())
                        : new java.util.HashSet<>())
                .documentUrls(entity.getDocumentUrls() != null ? new java.util.HashSet<>(entity.getDocumentUrls())
                        : new java.util.HashSet<>())
                .estPublie(entity.getEstPublie())
                .nbConsultations(entity.getNbConsultations())
                .typeFiche("ETABLISSEMENT")
                .adresse(entity.getAdresse())
                .ville(entity.getVille())

                .typeEtablissement(entity.getTypeEtablissement().name())
                .niveau(entity.getNiveau())
                .contacts(entity.getContacts())
                .siteWeb(entity.getSiteWeb())
                .offreFormation(entity.getOffreFormation())
                .estPublic(entity.getEstPublic())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .filieresProposees(entity.getFilieresProposees().stream()
                        .map(f -> FicheResponse.builder()
                                .trackingId(f.getTrackingId())
                                .titre(f.getTitre())
                                .resume(f.getResume())
                                .typeFiche("FILIERE")
                                .build())
                        .collect(Collectors.toSet()))
                .detailsSupplementaires(toDetailsSupplementaires(entity))
                .build();
    }

    /**
     * Construit le bloc optionnel de détails enrichis (XLSX).
     * Renvoie null si l'établissement n'a aucun champ enrichi renseigné —
     * permet au client de distinguer "pas couvert par XLSX" de "tout est null".
     */
    private EtablissementDetailsSupplementaires toDetailsSupplementaires(FicheEtablissement e) {
        EtablissementDetailsSupplementaires d = EtablissementDetailsSupplementaires.builder()
                .sigle(e.getSigle())
                .anneeCreation(e.getAnneeCreation())
                .statutJuridique(e.getStatutJuridique())
                .numeroAgrement(e.getNumeroAgrement())
                .dateAgrement(e.getDateAgrement())
                .autoriteTutelle(e.getAutoriteTutelle())
                .reconnaissanceCames(e.getReconnaissanceCames())
                .region(e.getRegion())
                .prefecture(e.getPrefecture())
                .commune(e.getCommune())
                .quartier(e.getQuartier())
                .lienGoogleMaps(e.getLienGoogleMaps())
                .presentation(e.getPresentation())
                .mission(e.getMission())
                .vision(e.getVision())
                .valeurs(e.getValeurs())
                .fraisInscription(e.getFraisInscription())
                .scolariteAnnuelle(e.getScolariteAnnuelle())
                .fraisDevise(e.getFraisDevise())
                .infrastructuresSynthese(e.getInfrastructuresSynthese())
                .nbBatiments(e.getNbBatiments())
                .nbAmphitheatres(e.getNbAmphitheatres())
                .bibliotheque(e.getBibliotheque())
                .wifi(e.getWifi())
                .internat(e.getInternat())
                .restaurant(e.getRestaurant())
                .cafeteria(e.getCafeteria())
                .terrainSport(e.getTerrainSport())
                .infirmerie(e.getInfirmerie())
                .parking(e.getParking())
                .nbEnseignants(e.getNbEnseignants())
                .nbEtudiants(e.getNbEtudiants())
                .nbDiplomes(e.getNbDiplomes())
                .tauxReussite(e.getTauxReussite())
                .tauxInsertionPro(e.getTauxInsertionPro())
                .universitesPartenaires(e.getUniversitesPartenaires())
                .entreprisesPartenaires(e.getEntreprisesPartenaires())
                .programmesMobilite(e.getProgrammesMobilite())
                .boursesInternes(e.getBoursesInternes())
                .boursesGouvernementales(e.getBoursesGouvernementales())
                .boursesInternationales(e.getBoursesInternationales())
                .clubs(e.getClubs())
                .sports(e.getSports())
                .noteMoyenneAvis(e.getNoteMoyenneAvis())
                .nbAvis(e.getNbAvis())
                .build();

        // Si TOUT est null, on renvoie null pour économiser la bande passante
        if (d.getSigle() == null && d.getRegion() == null && d.getInfrastructuresSynthese() == null
                && d.getPresentation() == null && d.getNbEtudiants() == null && d.getNbEnseignants() == null
                && d.getFraisInscription() == null && d.getScolariteAnnuelle() == null
                && d.getStatutJuridique() == null && d.getAutoriteTutelle() == null) {
            return null;
        }
        return d;
    }

    public void updateFromRequest(FicheEtablissementRequest request, FicheEtablissement entity,
            Set<FicheFiliere> filieres) {
        if (request == null)
            return;
        if (request.getTitre() != null)
            entity.setTitre(request.getTitre());
        if (request.getResume() != null)
            entity.setResume(request.getResume());
        if (request.getContenu() != null)
            entity.setContenu(request.getContenu());
        if (request.getEstPublie() != null)
            entity.setEstPublie(request.getEstPublie());
        if (request.getAdresse() != null)
            entity.setAdresse(request.getAdresse());
        if (request.getVille() != null)
            entity.setVille(request.getVille());

        if (request.getTypeEtablissement() != null)
            entity.setTypeEtablissement(parseTypeEtablissement(request.getTypeEtablissement()));
        if (request.getNiveau() != null)
            entity.setNiveau(request.getNiveau());
        if (request.getContacts() != null)
            entity.setContacts(request.getContacts());
        if (request.getSiteWeb() != null)
            entity.setSiteWeb(request.getSiteWeb());
        if (request.getOffreFormation() != null)
            entity.setOffreFormation(request.getOffreFormation());
        if (request.getEstPublic() != null)
            entity.setEstPublic(request.getEstPublic());
        if (request.getLatitude() != null)
            entity.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            entity.setLongitude(request.getLongitude());
        if (filieres != null)
            entity.setFilieresProposees(new java.util.HashSet<>(filieres));
    }

    private FicheEtablissement.TypeEtablissement parseTypeEtablissement(String rawType) {
        String normalized = rawType == null ? "" : rawType.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);
        return FicheEtablissement.TypeEtablissement.valueOf(normalized);
    }
}
