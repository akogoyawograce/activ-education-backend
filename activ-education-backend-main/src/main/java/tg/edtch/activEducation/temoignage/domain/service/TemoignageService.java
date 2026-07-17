package tg.edtch.activEducation.temoignage.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.temoignage.domain.dto.TemoignageRequest;
import tg.edtch.activEducation.temoignage.domain.dto.TemoignageResponse;
import tg.edtch.activEducation.temoignage.domain.entite.Temoignage;
import tg.edtch.activEducation.temoignage.repository.TemoignageRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class TemoignageService {

    private final TemoignageRepository repository;

    public TemoignageService(TemoignageRepository repository) {
        this.repository = repository;
    }

    public Page<TemoignageResponse> getTemoignagesPublies(int page, int size) {
        return repository.findByEstPublieTrueOrderByEstEnVedetteDescCreatedAtDesc(PageRequest.of(page, size))
            .map(this::toResponse);
    }

    public Page<TemoignageResponse> getTemoignagesParMetier(String metierTrackingId, int page, int size) {
        return repository.findByEstPublieTrueAndMetierTrackingIdOrderByCreatedAtDesc(metierTrackingId, PageRequest.of(page, size))
            .map(this::toResponse);
    }

    public Page<TemoignageResponse> getTemoignagesParFiliere(String filiereTrackingId, int page, int size) {
        return repository.findByEstPublieTrueAndFiliereTrackingIdOrderByCreatedAtDesc(filiereTrackingId, PageRequest.of(page, size))
            .map(this::toResponse);
    }

    public List<TemoignageResponse> getTemoignagesEnVedette() {
        return repository.findTop3ByEstPublieTrueAndEstEnVedetteTrueOrderByCreatedAtDesc()
            .stream().map(this::toResponse).toList();
    }

    public Page<TemoignageResponse> getAllTemoignages(int page, int size) {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
            .map(this::toResponse);
    }

    public TemoignageResponse creer(TemoignageRequest req) {
        var entity = Temoignage.builder()
            .auteurNom(req.auteurNom())
            .auteurPhotoUrl(req.auteurPhotoUrl())
            .auteurTitre(req.auteurTitre())
            .filiereSuivie(req.filiereSuivie())
            .etablissement(req.etablissement())
            .anneeParcours(req.anneeParcours())
            .contenu(req.contenu())
            .videoUrl(req.videoUrl())
            .metierTrackingId(req.metierTrackingId())
            .metierNom(req.metierNom())
            .filiereTrackingId(req.filiereTrackingId())
            .estPublie(req.estPublie() != null && req.estPublie())
            .estEnVedette(req.estEnVedette() != null && req.estEnVedette())
            .build();
        return toResponse(repository.save(entity));
    }

    public TemoignageResponse modifier(UUID trackingId, TemoignageRequest req) {
        var entity = repository.findByTrackingId(trackingId)
            .orElseThrow(() -> new NoSuchElementException("Témoignage introuvable"));
        entity.setAuteurNom(req.auteurNom());
        entity.setAuteurPhotoUrl(req.auteurPhotoUrl());
        entity.setAuteurTitre(req.auteurTitre());
        entity.setFiliereSuivie(req.filiereSuivie());
        entity.setEtablissement(req.etablissement());
        entity.setAnneeParcours(req.anneeParcours());
        entity.setContenu(req.contenu());
        entity.setVideoUrl(req.videoUrl());
        entity.setMetierTrackingId(req.metierTrackingId());
        entity.setMetierNom(req.metierNom());
        entity.setFiliereTrackingId(req.filiereTrackingId());
        if (req.estPublie() != null) entity.setEstPublie(req.estPublie());
        if (req.estEnVedette() != null) entity.setEstEnVedette(req.estEnVedette());
        return toResponse(repository.save(entity));
    }

    public void supprimer(UUID trackingId) {
        repository.deleteByTrackingId(trackingId);
    }

    public TemoignageResponse getTemoignage(UUID trackingId) {
        var entity = repository.findByTrackingId(trackingId)
            .orElseThrow(() -> new NoSuchElementException("Témoignage introuvable"));
        entity.setNbVues(entity.getNbVues() + 1);
        repository.save(entity);
        return toResponse(entity);
    }

    public long compterTemoignages() {
        return repository.countByEstPublieTrue();
    }

    private TemoignageResponse toResponse(Temoignage t) {
        return new TemoignageResponse(
            t.getTrackingId(), t.getAuteurNom(), t.getAuteurPhotoUrl(),
            t.getAuteurTitre(), t.getFiliereSuivie(), t.getEtablissement(),
            t.getAnneeParcours(), t.getContenu(), t.getVideoUrl(),
            t.getMetierTrackingId(), t.getMetierNom(), t.getFiliereTrackingId(),
            t.getEstPublie(), t.getEstEnVedette(), t.getNbVues(), t.getCreatedAt());
    }
}
