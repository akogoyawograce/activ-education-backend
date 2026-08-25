package tg.edtch.activEducation.prediction.application.service.serviceImple;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeRequest;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeResponse;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeSuiviResponse;
import tg.edtch.activEducation.prediction.application.mapper.OrientationOutcomeMapper;
import tg.edtch.activEducation.prediction.application.service.OrientationOutcomeService;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;
import tg.edtch.activEducation.prediction.domain.repository.OrientationOutcomeRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository ficheFiliereRepository;
    private final ObjectMapper objectMapper;

    /** Mapper construit une fois (il est stateless). */
    private OrientationOutcomeMapper mapper() {
        return new OrientationOutcomeMapper(objectMapper);
    }

    private Long resolveFiliereId(OrientationOutcomeRequest request) {
        if (request.getFiliereId() != null) {
            return request.getFiliereId();
        }
        return ficheFiliereRepository.findByTrackingId(request.getFiliereTrackingId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Filière introuvable : " + request.getFiliereTrackingId()))
                .getId();
    }

    @Override
    public OrientationOutcomeResponse creerOuMettreAJour(UUID eleveTrackingId,
                                                         OrientationOutcomeRequest request) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));

        // Résolution de la filière : id technique (backoffice) ou trackingId (mobile).
        final Long filiereId = resolveFiliereId(request);

        // Idempotence : on cherche un outcome existant pour (eleveId, filiereId)
        // avant d'en créer un nouveau. Si trouvé, on met à jour les champs
        // non-statut.
        Long eleveId = eleve.getId();
        List<OrientationOutcome> existants = repository.findByEleveId(eleveId).stream()
                .filter(o -> o.getFiliereId().equals(filiereId))
                .toList();

        OrientationOutcome entity;
        if (!existants.isEmpty()) {
            entity = existants.get(0);
            entity.setRiasecSnapshot(mapper().toEntity(request, filiereId, eleveId, LocalDate::now)
                    .getRiasecSnapshot());
            entity.setNotesSnapshot(mapper().toEntity(request, filiereId, eleveId, LocalDate::now)
                    .getNotesSnapshot());
            entity.setSerie(request.getSerie());
            if (request.getRegion() != null) {
                entity.setRegion(request.getRegion());
            }
            if (request.getOrdre() != null) {
                entity.setOrdre(request.getOrdre());
            }
            if (request.getSexe() != null) {
                entity.setSexe(request.getSexe());
            }
            if (request.getAnneeSession() != null) {
                entity.setAnneeSession(request.getAnneeSession());
            }
            if (request.getScoreRecommandation() != null) {
                entity.setScoreRecommandation(request.getScoreRecommandation());
            }
            log.info("OrientationOutcome mis à jour id={} eleve={} filiere={}",
                    entity.getId(), eleveTrackingId, request.getFiliereId());
        } else {
            entity = mapper().toEntity(request, filiereId, eleveId, LocalDate::now);
            log.info("OrientationOutcome créé eleve={} filiere={}",
                    eleveTrackingId, filiereId);
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

    @Override
    @Transactional(readOnly = true)
    public Page<OrientationOutcomeSuiviResponse> listerSuivi(
            OrientationOutcome.StatutOrientation statut, Pageable pageable) {
        Page<OrientationOutcome> page = statut != null
                ? repository.findByStatut(statut, pageable)
                : repository.findAllByOrderByDateChoixDesc(pageable);

        Map<Long, Eleve> eleves = page.getContent().stream()
                .map(o -> o.getEleveId()).distinct()
                .map(id -> eleveRepository.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(Eleve::getId, e -> e));

        Map<Long, String> filiereTitres = page.getContent().stream()
                .map(o -> o.getFiliereId()).distinct()
                .map(id -> ficheFiliereRepository.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(
                        f -> f.getId(), f -> f.getTitre()));

        return page.map(o -> {
            Eleve eleve = eleves.get(o.getEleveId());
            return OrientationOutcomeSuiviResponse.builder()
                    .trackingId(o.getTrackingId())
                    .eleveTrackingId(eleve != null ? eleve.getTrackingId() : null)
                    .eleveNom(eleve != null ? eleve.getNom() : null)
                    .elevePrenom(eleve != null ? eleve.getPrenom() : null)
                    .filiereTitre(filiereTitres.get(o.getFiliereId()))
                    .dateChoix(o.getDateChoix())
                    .serie(o.getSerie())
                    .region(o.getRegion())
                    .ordre(o.getOrdre())
                    .sexe(o.getSexe())
                    .anneeSession(o.getAnneeSession())
                    .scoreRecommandation(o.getScoreRecommandation())
                    .statut(o.getStatut() != null ? o.getStatut().name() : null)
                    .satisfaction(o.getSatisfaction())
                    .dateMajStatut(o.getDateMajStatut())
                    .commentaire(o.getCommentaire())
                    .createdAt(o.getCreatedAt())
                    .build();
        });
    }
}
