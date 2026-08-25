package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.EtablissementDetailsXlsxRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EtablissementDetailsXlsxResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.EtablissementDetailsXlsx;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.service.EtablissementDetailsXlsxService;
import tg.edtch.activEducation.bibliotheque.repository.EtablissementDetailsXlsxRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les détails étendus (xlsx) d'une fiche établissement.
 *
 * <p>
 * MVP Phase 2 sprint xlsx : tout en JSON, pas de structure typée. La
 * sérialisation passe par {@link ObjectMapper} avec pretty-print (pour
 * faciliter le debug).
 *
 * <p>
 * Si l'import xlsx devient volumineux (575 lignes de formations × 97 fiches),
 * on pourra basculer vers {@code etablissement_formation} en table dédiée.
 *
 * @see <a href="https://github.com/missions-Tog/ActivEducation/issues">Phase 2
 *      du sprint xlsx — 6 août 2026</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EtablissementDetailsXlsxServiceImpl implements EtablissementDetailsXlsxService {

        private final EtablissementDetailsXlsxRepository detailsRepository;
        private final FicheEtablissementRepository ficheEtablissementRepository;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional(readOnly = true)
        public EtablissementDetailsXlsxResponse getDetails(UUID trackingId) {
                FicheEtablissement fiche = findFicheOrThrow(trackingId);
                Optional<EtablissementDetailsXlsx> detailsOpt = detailsRepository.findByFicheId(fiche.getId());
                if (detailsOpt.isEmpty()) {
                        // Réponse "vide" : on renvoie juste le trackingId
                        return EtablissementDetailsXlsxResponse.builder()
                                        .ficheTrackingId(trackingId)
                                        .build();
                }
                return toResponse(detailsOpt.get());
        }

        @Override
        public EtablissementDetailsXlsxResponse upsertDetails(UUID trackingId,
                        EtablissementDetailsXlsxRequest request) {
                FicheEtablissement fiche = findFicheOrThrow(trackingId);
                EtablissementDetailsXlsx details = detailsRepository.findByFicheId(fiche.getId())
                                .orElseGet(() -> EtablissementDetailsXlsx.builder()
                                                .ficheId(fiche.getId())
                                                .build());
                details.setIdSourceXlsx(request.getIdSourceXlsx());
                try {
                        details.setDonneesEtenduesJson(objectMapper.writerWithDefaultPrettyPrinter()
                                        .writeValueAsString(request.getDonneesEtendues()));
                } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(
                                        "Impossible de sérialiser les données étendues en JSON : "
                                                        + e.getMessage(),
                                        e);
                }
                details.setDateExtraction(request.getDateExtraction() != null ? request.getDateExtraction()
                                : LocalDateTime.now());
                EtablissementDetailsXlsx saved = detailsRepository.save(details);
                detailsRepository.flush();
                log.info("Détails xlsx upsertés : fiche={}, idSourceXlsx={}", trackingId,
                                request.getIdSourceXlsx());
                return toResponse(saved);
        }

        @Override
        public void deleteDetails(UUID trackingId) {
                FicheEtablissement fiche = findFicheOrThrow(trackingId);
                detailsRepository.findByFicheId(fiche.getId())
                                .ifPresent(d -> {
                                        detailsRepository.delete(d);
                                        log.info("Détails xlsx supprimés : fiche={}", trackingId);
                                });
        }

        // ─────────────────────────────────────────────────────────────────────
        // Helpers
        // ─────────────────────────────────────────────────────────────────────

        private FicheEtablissement findFicheOrThrow(UUID trackingId) {
                return ficheEtablissementRepository.findByTrackingId(trackingId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Fiche établissement introuvable : trackingId="
                                                                + trackingId));
        }

        private EtablissementDetailsXlsxResponse toResponse(EtablissementDetailsXlsx d) {
                UUID trackingId = ficheEtablissementRepository.findById(d.getFicheId())
                                .map(FicheEtablissement::getTrackingId)
                                .orElse(null);
                return EtablissementDetailsXlsxResponse.builder()
                                .ficheTrackingId(trackingId)
                                .idSourceXlsx(d.getIdSourceXlsx())
                                .donneesEtenduesJson(d.getDonneesEtenduesJson())
                                .dateExtraction(d.getDateExtraction())
                                .build();
        }

        /**
         * Décode le JSON stocké (utile pour debug/import — non utilisé par
         * les endpoints actuels).
         */
        @SuppressWarnings("unused")
        private JsonNode parseJson(String raw) throws JsonProcessingException {
                return objectMapper.readTree(raw);
        }
}
