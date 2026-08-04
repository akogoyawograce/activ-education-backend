package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheGlobaleResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.domain.service.RechercheGlobaleService;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;
import tg.edtch.activEducation.shared.ai.service.AIEmbeddingService;
import tg.edtch.activEducation.shared.util.PgVectorLiteral;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service de recherche sémantique globale.
 * Pipeline RAG :
 * 1. Phrase utilisateur → vecteur via OpenAI Embedding.
 * 2. Recherche par similarité cosinus via pgvector sur toutes les fiches.
 * 3. Mapping polymorphe via instanceof pour identifier chaque type de fiche.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RechercheGlobaleServiceImpl implements RechercheGlobaleService {

    private final FicheRepository ficheRepository;
    private final AIEmbeddingService aiEmbeddingService;

    @Override
    public List<RechercheGlobaleResponse> rechercherFichesParPhrase(String phrase, int limite) {
        log.debug("Recherche globale sémantique pour phrase='{}', limite={}", phrase, limite);

        // 1. Convertir la phrase de l'utilisateur en vecteur
        float[] vecteurRequete = aiEmbeddingService.generateEmbedding(phrase);

        // 2. Requête native pgvector → liste d'IDs ordonnés par pertinence.
        // Le repo attend un littéral pgvector (String), pas un float[].
        String vecteurLiteral = PgVectorLiteral.toVectorLiteral(vecteurRequete);
        List<Long> ids = ficheRepository.rechercherIdsParSimilariteGlobale(vecteurLiteral, limite);
        if (ids.isEmpty()) {
            return List.of();
        }

        // 3. Chargement polymorphe des entités en conservant l'ordre pgvector
        // trouverParIdsOrdonnes gère jusqu'à 10 résultats ; pour plus, on trie en Java
        List<Fiche> fiches = (ids.size() <= 10)
                ? ficheRepository.trouverParIdsOrdonnes(ids)
                : ficheRepository.findAllById(ids).stream()
                        .sorted(Comparator.comparingInt(f -> ids.indexOf(f.getId())))
                        .collect(Collectors.toList());

        // 4. Mapper chaque fiche vers le DTO unifié
        return fiches.stream()
                .map(this::mapperVersResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mappe une entité Fiche (polymorphe) vers le DTO de réponse unifié.
     * Utilise instanceof pour déterminer le typeResultat de façon sûre.
     */
    private RechercheGlobaleResponse mapperVersResponse(Fiche fiche) {
        // Détermination du type selon la sous-classe concrète
        String typeResultat;
        if (fiche instanceof FicheMetier) {
            typeResultat = "METIER";
        } else if (fiche instanceof FicheFiliere) {
            typeResultat = "FILIERE";
        } else if (fiche instanceof FicheEtablissement) {
            typeResultat = "ETABLISSEMENT";
        } else if (fiche instanceof FicheSerie) {
            typeResultat = "SERIE";
        } else {
            typeResultat = "INCONNU";
        }

        // Récupère la première image de couverture si disponible
        String imageCouverture = (fiche.getImageUrls() != null && !fiche.getImageUrls().isEmpty())
                ? fiche.getImageUrls().iterator().next()
                : null;

        return RechercheGlobaleResponse.builder()
                .trackingId(fiche.getTrackingId())
                .typeResultat(typeResultat)
                .titre(fiche.getTitre())
                .resume(fiche.getResume())
                .imageCouverture(imageCouverture)
                .build();
    }
}
