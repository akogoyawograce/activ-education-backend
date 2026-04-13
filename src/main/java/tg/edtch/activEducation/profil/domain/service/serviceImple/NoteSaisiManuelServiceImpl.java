package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.NoteSaisiManuelRequest;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.application.mapper.NoteSaisiManuelMapper;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;
import tg.edtch.activEducation.profil.domain.service.NoteSaisiManuelService;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.NoteSaisiManuelRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service NoteSaisiManuel.
 * Toutes les opérations utilisent des trackingId UUID publics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoteSaisiManuelServiceImpl implements NoteSaisiManuelService {

    private final NoteSaisiManuelRepository noteRepository;
    private final EleveRepository eleveRepository;
    private final NoteSaisiManuelMapper noteMapper;

    @Override
    public NoteSaisiManuelResponse ajouterNote(UUID eleveTrackingId, NoteSaisiManuelRequest request) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable pour le trackingId : " + eleveTrackingId));

        NoteSaisiManuel note = noteMapper.toEntity(request, eleve);
        NoteSaisiManuel saved = noteRepository.save(note);
        log.info("Note ajoutée : élève={} matière={} note={} trackingId={}",
                eleveTrackingId, saved.getMatiere(), saved.getNote(), saved.getTrackingId());
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteSaisiManuelResponse getNote(UUID trackingId) {
        return noteMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteSaisiManuelResponse> getNotesByEleve(UUID eleveTrackingId) {
        return noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId)
                .stream()
                .map(noteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteSaisiManuelResponse> getNotesByElevePagine(UUID eleveTrackingId, Pageable pageable) {
        return noteRepository.findByEleveTrackingId(eleveTrackingId, pageable)
                .map(noteMapper::toResponse);
    }

    @Override
    public NoteSaisiManuelResponse modifierNote(UUID trackingId, NoteSaisiManuelRequest request) {
        NoteSaisiManuel note = findOrThrow(trackingId);
        noteMapper.updateFromRequest(request, note);
        NoteSaisiManuel saved = noteRepository.save(note);
        log.info("Note modifiée : trackingId={}", trackingId);
        return noteMapper.toResponse(saved);
    }

    @Override
    public void supprimerNote(UUID trackingId) {
        NoteSaisiManuel note = findOrThrow(trackingId);
        noteRepository.delete(note);
        log.info("Note supprimée (hard-delete) : trackingId={}", trackingId);
    }

    // ─── Helper privé ─────────────────────────────────────────────────────────
    private NoteSaisiManuel findOrThrow(UUID trackingId) {
        return noteRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note introuvable pour le trackingId : " + trackingId));
    }
}
