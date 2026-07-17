package tg.edtch.activEducation.portfolio.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;
import tg.edtch.activEducation.portfolio.domain.dto.AnalysePortfolioResponse;
import tg.edtch.activEducation.portfolio.domain.dto.CompetenceRequest;
import tg.edtch.activEducation.portfolio.domain.dto.CompetenceResponse;
import tg.edtch.activEducation.portfolio.domain.entite.PortfolioCompetence;
import tg.edtch.activEducation.portfolio.repository.PortfolioCompetenceRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioService {

    private final PortfolioCompetenceRepository repository;
    private final FicheMetierRepository metierRepository;

    public PortfolioService(PortfolioCompetenceRepository repository,
                            FicheMetierRepository metierRepository) {
        this.repository = repository;
        this.metierRepository = metierRepository;
    }

    public List<CompetenceResponse> listerCompetences(String eleveTrackingId) {
        return repository.findByEleveTrackingIdOrderByCategorieAscNiveauEstimeDesc(eleveTrackingId)
            .stream().map(this::toResponse).toList();
    }

    public CompetenceResponse ajouterCompetence(String eleveTrackingId, CompetenceRequest req) {
        var entity = PortfolioCompetence.builder()
            .eleveTrackingId(eleveTrackingId)
            .titre(req.titre())
            .description(req.description())
            .categorie(req.categorie())
            .niveauEstime(req.niveauEstime())
            .source(req.source())
            .estVisible(req.estVisible() != null ? req.estVisible() : true)
            .build();
        return toResponse(repository.save(entity));
    }

    public CompetenceResponse modifierCompetence(UUID trackingId, CompetenceRequest req) {
        var entity = repository.findByTrackingId(trackingId)
            .orElseThrow(() -> new NoSuchElementException("Compétence introuvable"));
        entity.setTitre(req.titre());
        entity.setDescription(req.description());
        entity.setCategorie(req.categorie());
        entity.setNiveauEstime(req.niveauEstime());
        entity.setSource(req.source());
        if (req.estVisible() != null) entity.setEstVisible(req.estVisible());
        return toResponse(repository.save(entity));
    }

    public void supprimerCompetence(UUID trackingId) {
        repository.deleteByTrackingId(trackingId);
    }

    public AnalysePortfolioResponse analyser(String eleveTrackingId) {
        var competences = repository.findByEleveTrackingIdOrderByCategorieAscNiveauEstimeDesc(eleveTrackingId);

        var repartition = competences.stream()
            .collect(Collectors.groupingBy(
                PortfolioCompetence::getCategorie,
                Collectors.summingInt(PortfolioCompetence::getNiveauEstime)
            ));

        double scoreGlobal = competences.isEmpty() ? 0 :
            competences.stream().mapToInt(PortfolioCompetence::getNiveauEstime).average().orElse(0) / 5.0 * 100;

        var titresCompetences = competences.stream()
            .map(c -> c.getTitre().toLowerCase())
            .collect(Collectors.toSet());

        var metiers = metierRepository.findAll();
        var recommandations = metiers.stream()
            .map(m -> evaluerMetier(m, titresCompetences))
            .sorted((a, b) -> Double.compare(b.scoreCompatibilite(), a.scoreCompatibilite()))
            .limit(5)
            .toList();

        return new AnalysePortfolioResponse(repartition, competences.size(), Math.round(scoreGlobal * 100.0) / 100.0, recommandations);
    }

    private AnalysePortfolioResponse.RecommandationMetier evaluerMetier(FicheMetier metier, Set<String> competencesEleve) {
        var motsCles = extraireMotsCles(metier);
        int requis = motsCles.size();
        if (requis == 0) {
            return new AnalysePortfolioResponse.RecommandationMetier(
                metier.getTitre(), metier.getTrackingId().toString(), 0, 0, 0, List.of()
            );
        }
        var manquants = new ArrayList<String>();
        int acquis = 0;
        for (var mot : motsCles) {
            if (competencesEleve.stream().anyMatch(c -> c.contains(mot.toLowerCase()))) {
                acquis++;
            } else {
                manquants.add(mot);
            }
        }
        double score = (double) acquis / requis * 100;
        return new AnalysePortfolioResponse.RecommandationMetier(
            metier.getTitre(), metier.getTrackingId().toString(), Math.round(score * 100.0) / 100.0, acquis, requis, manquants
        );
    }

    private List<String> extraireMotsCles(FicheMetier metier) {
        var mots = new ArrayList<String>();
        if (metier.getMissions() != null) {
            var stopWords = Set.of("le", "la", "les", "de", "des", "du", "et", "ou", "un", "une", "dans",
                "pour", "sur", "avec", "par", "est", "sont", "qui", "que", "pas", "plus", "aux", "ces",
                "cette", "ses", "leur", "leurs", "entre", "sous", "comme", "mais", "tout", "tous");
            mots.addAll(Arrays.stream(metier.getMissions().toLowerCase()
                    .replaceAll("[^a-zàâçéèêëîïôûùüÿæœ\\s]", " ")
                    .split("\\s+"))
                .filter(m -> m.length() > 4 && !stopWords.contains(m))
                .distinct()
                .toList());
        }
        if (metier.getCompetences() != null) {
            mots.addAll(List.of(metier.getCompetences().toLowerCase().split(",\\s*")));
        }
        return mots.stream().distinct().toList();
    }

    private CompetenceResponse toResponse(PortfolioCompetence e) {
        return new CompetenceResponse(
            e.getTrackingId(), e.getTitre(), e.getDescription(), e.getCategorie(),
            e.getNiveauEstime(), e.getSource(), e.getEstVisible(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
