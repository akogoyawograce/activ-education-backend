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
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;

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
    private final GeminiEmbeddingService geminiEmbeddingService;

    @Override
    public EntreeFAQResponse creerEntree(EntreeFAQRequest request) {
        if (faqRepository.existsByQuestion(request.getQuestion())) {
            throw new IllegalArgumentException("Une entrée FAQ avec cette question existe déjà.");
        }
        EntreeFAQ faq = faqMapper.toEntity(request);
        // Génération de l'embedding via Gemini (peut échouer si clé invalide)
        try {
            faq.setEmbedding(geminiEmbeddingService.generateEmbedding(request.getQuestion() + " " + request.getReponse()));
        } catch (Exception e) {
            log.warn("Impossible de générer l'embedding pour la FAQ : {}", e.getMessage());
        }
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
        // Regénération de l'embedding suite à la modification
        try {
            faq.setEmbedding(geminiEmbeddingService.generateEmbedding(request.getQuestion() + " " + request.getReponse()));
        } catch (Exception e) {
            log.warn("Impossible de régénérer l'embedding pour la FAQ : {}", e.getMessage());
        }
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

    @Override
    @Transactional(readOnly = true)
    public tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheIAResponse rechercherParIA(
            String questionUser, int limite) {
        // 1. Recherche les entrées par similarité
        List<EntreeFAQ> faqs;
        try {
            float[] queryEmbedding = geminiEmbeddingService.generateEmbedding(questionUser);
            faqs = faqRepository.rechercherParSimilarite(queryEmbedding, limite);
        } catch (Exception e) {
            log.warn("Impossible de faire la recherche sémantique : {}", e.getMessage());
            faqs = List.of();
        }

        List<EntreeFAQResponse> sources = faqs.stream()
                .map(faqMapper::toResponse)
                .collect(Collectors.toList());

        // 2. Formatage des contextes pour l'IA
        List<String> contextes = faqs.stream()
                .map(faq -> "Q: " + faq.getQuestion() + " | R: " + faq.getReponse())
                .collect(Collectors.toList());

        // 3. Appel de l'IA pour synthétiser
        String reponseIA = "Désolé, je n'ai trouvé aucune information à ce sujet.";
        if (!contextes.isEmpty()) {
            reponseIA = geminiEmbeddingService.generateAnswer(questionUser, contextes);
        }

        return tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheIAResponse.builder()
                .reponseIA(reponseIA)
                .sourcesUtilisees(sources)
                .build();
    }

    private EntreeFAQ findOrThrow(UUID trackingId) {
        return faqRepository.findByTrackingId(trackingId)
                .orElseThrow(
                        () -> new NoSuchElementException("EntreeFAQ introuvable pour le trackingId : " + trackingId));
    }
}
