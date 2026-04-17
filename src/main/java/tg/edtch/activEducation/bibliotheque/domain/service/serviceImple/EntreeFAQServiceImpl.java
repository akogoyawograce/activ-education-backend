package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.EntreeFAQRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EntreeFAQResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.EntreeFAQMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.EntreeFAQ;
import tg.edtch.activEducation.bibliotheque.domain.service.EntreeFAQService;
import tg.edtch.activEducation.bibliotheque.repository.EntreeFAQRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EntreeFAQServiceImpl implements EntreeFAQService {

    private final EntreeFAQRepository faqRepository;
    private final EntreeFAQMapper faqMapper;

    @Override
    public EntreeFAQResponse creerEntree(EntreeFAQRequest request) {
        if (faqRepository.existsByQuestion(request.getQuestion())) {
            throw new IllegalArgumentException("Une entrée FAQ avec cette question existe déjà.");
        }
        EntreeFAQ faq = faqMapper.toEntity(request);
        EntreeFAQ saved = faqRepository.save(faq);
        log.info("Nouvelle entrée FAQ créée : trackingId={}", saved.getTrackingId());
        return faqMapper.toResponse(saved);
    }

    @Override
    public EntreeFAQResponse getEntree(UUID trackingId) {
        EntreeFAQ faq = findOrThrow(trackingId);
        // Incrémentation du nombre de vues
        faq.setNbVues(faq.getNbVues() + 1);
        return faqMapper.toResponse(faq);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntreeFAQResponse> listerToutes(Pageable pageable) {
        return faqRepository.findAllByEstPublieTrue(pageable)
                .map(faqMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntreeFAQResponse> listerParCategorie(String categorie) {
        return faqRepository.findByCategorieAndEstPublieTrue(categorie).stream()
                .map(faqMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EntreeFAQResponse modifierEntree(UUID trackingId, EntreeFAQRequest request) {
        EntreeFAQ faq = findOrThrow(trackingId);
        faqMapper.updateFromRequest(request, faq);
        EntreeFAQ saved = faqRepository.save(faq);
        log.info("Entrée FAQ modifiée : trackingId={}", trackingId);
        return faqMapper.toResponse(saved);
    }

    @Override
    public void supprimerEntree(UUID trackingId) {
        EntreeFAQ faq = findOrThrow(trackingId);
        faqRepository.delete(faq);
        log.info("Entrée FAQ supprimée : trackingId={}", trackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listerCategories() {
        return faqRepository.findAllCategories();
    }

    private EntreeFAQ findOrThrow(UUID trackingId) {
        return faqRepository.findByTrackingId(trackingId)
                .orElseThrow(
                        () -> new NoSuchElementException("EntreeFAQ introuvable pour le trackingId : " + trackingId));
    }
}
