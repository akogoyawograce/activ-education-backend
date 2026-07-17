package tg.edtch.activEducation.badge.domain.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.badge.domain.dto.BadgeResponse;
import tg.edtch.activEducation.badge.domain.entite.Badge;
import tg.edtch.activEducation.badge.domain.entite.BadgeDecerne;
import tg.edtch.activEducation.badge.repository.BadgeDecerneRepository;
import tg.edtch.activEducation.badge.repository.BadgeRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.portfolio.repository.PortfolioCompetenceRepository;
import tg.edtch.activEducation.reseau.repository.PublicationReseauRepository;
import tg.edtch.activEducation.entretien.repository.SimulationEntretienRepository;

import org.springframework.data.domain.PageRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final BadgeDecerneRepository badgeDecerneRepository;
    private final PortfolioCompetenceRepository portfolioRepository;
    private final PublicationReseauRepository publicationRepository;
    private final SimulationEntretienRepository entretienRepository;

    public BadgeService(BadgeRepository badgeRepository,
                        BadgeDecerneRepository badgeDecerneRepository,
                        PortfolioCompetenceRepository portfolioRepository,
                        PublicationReseauRepository publicationRepository,
                        SimulationEntretienRepository entretienRepository) {
        this.badgeRepository = badgeRepository;
        this.badgeDecerneRepository = badgeDecerneRepository;
        this.portfolioRepository = portfolioRepository;
        this.publicationRepository = publicationRepository;
        this.entretienRepository = entretienRepository;
    }

    @PostConstruct
    public void initialiserBadges() {
        var badges = List.of(
            Map.of("code", "PREMIER_QUIZ", "nom", "Pionnier", "description", "Premier quiz complété",
                "icone", "quiz", "categorie", "Quiz", "condition", "Compléter votre premier quiz", "ordre", 1),
            Map.of("code", "EXPLORATEUR", "nom", "Explorateur", "description", "5 fiches consultées",
                "icone", "explore", "categorie", "Découverte", "condition", "Consulter 5 fiches d'orientation", "ordre", 2),
            Map.of("code", "POLYGLOTTE", "nom", "Polyglotte", "description", "3 langues dans le portfolio",
                "icone", "language", "categorie", "Portfolio", "condition", "Ajouter 3 langues à votre portfolio", "ordre", 3),
            Map.of("code", "REVEUR", "nom", "Rêveur", "description", "Métier souhaité renseigné",
                "icone", "star", "categorie", "Profil", "condition", "Renseigner votre métier souhaité", "ordre", 4),
            Map.of("code", "RESEAU", "nom", "Réseau", "description", "3 publications partagées",
                "icone", "group", "categorie", "Social", "condition", "Publier 3 messages sur le réseau", "ordre", 5),
            Map.of("code", "PERSEVERANT", "nom", "Persévérant", "description", "3 entretiens simulés",
                "icone", "mic", "categorie", "Entretien", "condition", "Réaliser 3 simulations d'entretien", "ordre", 6),
            Map.of("code", "CURIEUX", "nom", "Curieux", "description", "3 filières explorées",
                "icone", "search", "categorie", "Exploration", "condition", "Explorer 3 filières différentes", "ordre", 7),
            Map.of("code", "COMPETENCES", "nom", "Compétent", "description", "10 compétences dans le portfolio",
                "icone", "skill", "categorie", "Portfolio", "condition", "Ajouter 10 compétences à votre portfolio", "ordre", 8)
        );

        for (var b : badges) {
            if (badgeRepository.findByCode((String) b.get("code")).isEmpty()) {
                badgeRepository.save(Badge.builder()
                    .code((String) b.get("code"))
                    .nom((String) b.get("nom"))
                    .description((String) b.get("description"))
                    .icone((String) b.get("icone"))
                    .categorie((String) b.get("categorie"))
                    .conditionExplication((String) b.get("condition"))
                    .ordreAffichage((Integer) b.get("ordre"))
                    .build());
            }
        }
    }

    public List<BadgeResponse> getBadgesEleve(String eleveTrackingId) {
        var badges = badgeRepository.findAllByOrderByOrdreAffichageAsc();
        var obtenus = badgeDecerneRepository.findByEleveTrackingIdOrderByDateObtentionDesc(eleveTrackingId);
        var obtenusMap = obtenus.stream()
            .collect(Collectors.toMap(BadgeDecerne::getBadgeTrackingId, b -> b));

        return badges.stream()
            .map(b -> {
                var decerne = obtenusMap.get(b.getTrackingId().toString());
                return new BadgeResponse(
                    b.getTrackingId(), b.getCode(), b.getNom(), b.getDescription(),
                    b.getIcone(), b.getCategorie(), b.getConditionExplication(),
                    decerne != null,
                    decerne != null ? decerne.getDateObtention() : null,
                    obtenus.size()
                );
            })
            .toList();
    }

    public int getTotalBadges(String eleveTrackingId) {
        return badgeDecerneRepository.countByEleveTrackingId(eleveTrackingId);
    }

    public List<BadgeResponse> verifierEtAttribuer(String eleveTrackingId) {
        var badges = badgeRepository.findAllByOrderByOrdreAffichageAsc();
        var obtenus = badgeDecerneRepository.findByEleveTrackingIdOrderByDateObtentionDesc(eleveTrackingId);
        var codesObtenus = obtenus.stream()
            .map(d -> badgeRepository.findByTrackingId(UUID.fromString(d.getBadgeTrackingId()))
                .map(Badge::getCode).orElse(""))
            .collect(Collectors.toSet());

        var nouveaux = new ArrayList<BadgeResponse>();

        for (var badge : badges) {
            if (codesObtenus.contains(badge.getCode())) continue;

            boolean conditionRemplie = verifierCondition(eleveTrackingId, badge.getCode());
            if (conditionRemplie) {
                badgeDecerneRepository.save(BadgeDecerne.builder()
                    .eleveTrackingId(eleveTrackingId)
                    .badgeTrackingId(badge.getTrackingId().toString())
                    .build());
                nouveaux.add(new BadgeResponse(
                    badge.getTrackingId(), badge.getCode(), badge.getNom(), badge.getDescription(),
                    badge.getIcone(), badge.getCategorie(), badge.getConditionExplication(),
                    true, null, 0));
            }
        }

        return nouveaux;
    }

    private boolean verifierCondition(String eleveId, String code) {
        return switch (code) {
            case "POLYGLOTTE" -> portfolioRepository.findByEleveTrackingIdAndCategorie(eleveId, "Langue").size() >= 3;
            case "COMPETENCES" -> portfolioRepository.findByEleveTrackingIdOrderByCategorieAscNiveauEstimeDesc(eleveId).size() >= 10;
            case "RESEAU" -> publicationRepository.findByAuteurTrackingIdOrderByCreatedAtDesc(eleveId, PageRequest.of(0, 100)).getSize() >= 3;
            case "PERSEVERANT" -> entretienRepository.findByEleveTrackingIdOrderByCreatedAtDesc(eleveId).size() >= 3;
            default -> false;
        };
    }
}
