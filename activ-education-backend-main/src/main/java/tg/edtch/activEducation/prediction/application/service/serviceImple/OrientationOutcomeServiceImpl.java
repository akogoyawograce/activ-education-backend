package tg.edtch.activEducation.prediction.application.service.serviceImple;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeRequest;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeResponse;
import tg.edtch.activEducation.prediction.application.mapper.OrientationOutcomeMapper;
import tg.edtch.activEducation.prediction.application.service.OrientationOutcomeService;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;
import tg.edtch.activEducation.prediction.domain.repository.OrientationOutcomeRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service d'orientation outcomes.
 *
 * <p>Pour la Phase 2, on persiste le outcome tel quel (les scores sont
 * calculés ailleurs — soit par le mobile avant l'appel, soit par le moteur
 * de la Phase 3). Le mapping des Map → JSONB est délégué au mapper.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrientationOutcomeServiceImpl implements OrientationOutcomeService {

    private final OrientationOutcomeRepository repository;
    private final EleveRepository eleveRepository;
    private final ObjectMapper objectMapper;

    /** Mapper construit une fois (il est stateless). */
    private OrientationOutcomeMapper mapper() {
        return new OrientationOutcomeMapper(objectMapper);
    }

    @Override
    public OrientationOutcomeResponse creerOuMettreAJour(UUID eleveTrackingId,
                                                         OrientationOutcomeRequest request) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));

        // Idempotence : on cherche un outcome existant pour (eleveId, filiereId)
        // avant d'en créer un nouveau. Si trouvé, on met à jour les champs
        // non-statut.
        Long eleveId = eleve.getId();
        List<OrientationOutcome> existants = repository.findByEleveId(eleveId).stream()
                .filter(o -> o.getFiliereId().equals(request.getFiliereId()))
                .toList();

        OrientationOutcome entity;
        if (!existants.isEmpty()) {
            entity = existants.get(0);
            entity.setRiasecSnapshot(mapper().toEntity(request, eleveId, LocalDate::now)
                    .getRiasecSnapshot());
            entity.setNotesSnapshot(mapper().toEntity(request, eleveId, LocalDate::now)
                    .getNotesSnapshot());
            entity.setSerie(request.getSerie());
            if (request.getScoreRecommandation() != null) {
                entity.setScoreRecommandation(request.getScoreRecommandation());
            }
            log.info("OrientationOutcome mis à jour id={} eleve={} filiere={}",
                    entity.getId(), eleveTrackingId, request.getFiliereId());
        } else {
            entity = mapper().toEntity(request, eleveId, LocalDate::now);
            log.info("OrientationOutcome créé eleve={} filiere={}",
                    eleveTrackingId, request.getFiliereId());
        }

        OrientationOutcome saved = repository.save(entity);
        return mapper().toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrientationOutcomeResponse> listerParEleve(UUID eleveTrackingId) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));
        return repository.findByEleveId(eleve.getId()).stream()
                .map(mapper()::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrientationOutcomeResponse mettreAJourStatut(UUID outcomeTrackingId,
                                                       String statut,
                                                       Integer satisfaction,
                                                       String commentaire) {
        OrientationOutcome entity = repository.findByTrackingId(outcomeTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Outcome introuvable : " + outcomeTrackingId));

        if (statut != null) {
            try {
                entity.setStatut(OrientationOutcome.StatutOrientation.valueOf(statut));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Statut invalide : " + statut
                        + " (attendu : EN_COURS, ADMIS, RECALE, ABANDON, REORIENTE)");
            }
        }
        if (satisfaction != null) {
            if (satisfaction < 1 || satisfaction > 5) {
                throw new IllegalArgumentException("La satisfaction doit être entre 1 et 5");
            }
            entity.setSatisfaction(satisfaction);
        }
        if (commentaire != null) {
            entity.setCommentaire(commentaire);
        }
        if (statut != null) {
            entity.setDateMajStatut(LocalDate.now());
        }

        log.info("OrientationOutcome statut maj id={} → {}", entity.getId(), statut);
        return mapper().toResponse(repository.save(entity));
    }
}
