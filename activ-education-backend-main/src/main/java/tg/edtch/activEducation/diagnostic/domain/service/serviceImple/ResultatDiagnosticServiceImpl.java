package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.request.ResultatDiagnosticRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.ResultatDiagnosticMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;
import tg.edtch.activEducation.diagnostic.domain.service.ResultatDiagnosticService;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResultatDiagnosticServiceImpl implements ResultatDiagnosticService {

    private final ResultatDiagnosticRepository resultatRepository;
    private final EleveRepository eleveRepository;
    private final QuizRepository quizRepository;
    private final ResultatDiagnosticMapper resultatMapper;

    @Override
    public ResultatDiagnosticResponse enregistrerResultat(ResultatDiagnosticRequest request) {
        Eleve eleve = eleveRepository.findByTrackingId(request.getEleveTrackingId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable pour le trackingId : " + request.getEleveTrackingId()));

        Quiz quiz = quizRepository.findByTrackingId(request.getQuizTrackingId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Quiz introuvable pour le trackingId : " + request.getQuizTrackingId()));

        if (!quiz.getEstActif()) {
            throw new IllegalStateException("Impossible d'enregistrer un résultat pour un quiz désactivé.");
        }

        ResultatDiagnostic resultat = resultatMapper.toEntity(request, eleve, quiz);
        ResultatDiagnostic saved = resultatRepository.save(resultat);
        log.info("Résultat de diagnostic enregistré : élève={} quiz={} profil='{}' score={} trackingId={}",
                request.getEleveTrackingId(), request.getQuizTrackingId(),
                saved.getProfilDecouvert(), saved.getScoreFinal(), saved.getTrackingId());
        return resultatMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultatDiagnosticResponse getResultat(UUID trackingId) {
        return resultatMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResultatDiagnosticResponse> getResultatsEleve(UUID eleveTrackingId, Pageable pageable) {
        return resultatRepository
                .findByEleveTrackingIdOrderByDatePassageDesc(eleveTrackingId, pageable)
                .map(resultatMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultatDiagnosticResponse> getDernierResultat(UUID eleveTrackingId, UUID quizTrackingId) {
        return resultatRepository
                .findFirstByEleveTrackingIdAndQuizTrackingIdOrderByDatePassageDesc(eleveTrackingId, quizTrackingId)
                .map(resultatMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultatDiagnosticResponse> getDernierResultat(UUID eleveTrackingId) {
        return resultatRepository
                .findFirstByEleveTrackingIdOrderByDatePassageDesc(eleveTrackingId)
                .map(resultatMapper::toResponse);
    }

    @Override
    public void supprimerResultat(UUID trackingId) {
        ResultatDiagnostic resultat = findOrThrow(trackingId);
        resultatRepository.delete(resultat);
        log.warn("Résultat de diagnostic supprimé (admin) : trackingId={}", trackingId);
    }

    private ResultatDiagnostic findOrThrow(UUID trackingId) {
        return resultatRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Résultat introuvable pour le trackingId : " + trackingId));
    }
}
