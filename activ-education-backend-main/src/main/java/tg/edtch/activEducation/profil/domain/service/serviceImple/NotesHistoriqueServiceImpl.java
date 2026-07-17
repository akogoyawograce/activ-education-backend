package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.NotesHistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotesHistoriqueResponse;
import tg.edtch.activEducation.profil.application.mapper.NotesHistoriqueMapper;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NotesHistorique;
import tg.edtch.activEducation.profil.domain.repository.NotesHistoriqueRepository;
import tg.edtch.activEducation.profil.domain.service.NotesHistoriqueService;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation de {@link NotesHistoriqueService}.
 *
 * <p>La résolution {@code eleveTrackingId} → entité {@link Eleve} se fait ici,
 * pas dans le mapper : le mapper reste agnostique de la persistance.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotesHistoriqueServiceImpl implements NotesHistoriqueService {

    private final NotesHistoriqueRepository repository;
    private final EleveRepository eleveRepository;

    @Override
    public NotesHistoriqueResponse ajouter(UUID eleveTrackingId, NotesHistoriqueRequest request) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));

        NotesHistorique entity = NotesHistoriqueMapper.toEntity(request);
        entity.setEleve(eleve);

        NotesHistorique saved = repository.save(entity);
        log.info("NotesHistorique créée id={} eleve={} annee={}",
                saved.getId(), eleveTrackingId, request.getAnneeScolaire());
        return NotesHistoriqueMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotesHistoriqueResponse get(UUID trackingId) {
        return repository.findByTrackingId(trackingId)
                .map(NotesHistoriqueMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note historique introuvable : " + trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotesHistoriqueResponse> listerParEleve(UUID eleveTrackingId) {
        // Résoudre l'élève d'abord (404 explicite si pas trouvé).
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));
        return repository.findByEleveIdOrderByAnneeScolaireDesc(eleve.getId())
                .stream()
                .map(NotesHistoriqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotesHistoriqueResponse> listerMoyennesGenerales(UUID eleveTrackingId) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));
        return repository.findByEleveIdAndEstMoyenneGeneraleTrueOrderByAnneeScolaireDesc(
                        eleve.getId())
                .stream()
                .map(NotesHistoriqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimer(UUID trackingId) {
        NotesHistorique entity = repository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Note historique introuvable : " + trackingId));
        repository.delete(entity);
        log.info("NotesHistorique supprimée id={}", entity.getId());
    }
}
