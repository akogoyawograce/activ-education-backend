package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheFiliereMapper;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheSerieMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheExplorerPersonnaliseeService;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;
import tg.edtch.activEducation.prediction.domain.util.ProfilFiliereRiasecCatalog;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.riasec.domain.entite.TestRIASECResultat;
import tg.edtch.activEducation.riasec.repository.TestRIASECResultatRepository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du tri personnalisé de l'Explorer.
 *
 * <p>Le scoring réutilise la logique de similarité RIASEC existante
 * (cosinus vecteur élève × {@link ProfilFiliereRiasecCatalog#profilPour}),
 * identique à celle du moteur 3 signaux
 * ({@code Recommandation3SignauxServiceImpl.calculerAspiration}). Elle est
 * complétée par un score de mots-clés déterministe (filière actuelle,
 * métier souhaité, matières préférées) quand le RIASEC est absent ou
 * insuffisant pour différencier.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FicheExplorerPersonnaliseeServiceImpl implements FicheExplorerPersonnaliseeService {

    /** Vecteur neutre utilisé en l'absence de test RIASEC. */
    private static final List<Double> RIASEC_NEUTRE = List.of(0.5, 0.5, 0.5, 0.5, 0.5, 0.5);

    private static final Sort TRI_GENERIQUE = Sort.by(Sort.Direction.DESC, "createdAt");

    private final EleveRepository eleveRepository;
    private final TestRIASECResultatRepository riasecRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheSerieRepository serieRepository;
    private final FicheFiliereMapper filiereMapper;
    private final FicheSerieMapper serieMapper;

    // ─────────────────────────────────────────────────────────────────────
    // API publique
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public Page<FicheFiliereResponse> listerFilieresPersonnalisees(UUID eleveTrackingId, Pageable pageable) {
        Optional<ProfilExplorer> profil = construireProfil(eleveTrackingId);
        if (profil.isEmpty()) {
            // Repli générique : tri createdAt DESC, jamais vide ni en erreur.
            return filiereRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), TRI_GENERIQUE))
                    .map(filiereMapper::toResponse);
        }
        List<FicheFiliere> toutes = filiereRepository.findAll();
        List<ScoredFiche<FicheFiliere>> scorees = toutes.stream()
                .map(f -> new ScoredFiche<>(scoreFiliere(f, profil.get()), f.getCreatedAt(), f))
                .sorted(comparateurScore())
                .collect(Collectors.toList());
        log.info("Explorer personnalisé : {} filières scorées pour élève {} (RIASEC={}, {} mots-clés)",
                scorees.size(), eleveTrackingId, profil.get().riasec() != null, profil.get().tokens().size());
        return paginer(scorees, pageable).map(filiereMapper::toResponse);
    }

    @Override
    public Page<FicheSerieResponse> listerSeriesPersonnalisees(UUID eleveTrackingId, Pageable pageable) {
        Optional<ProfilExplorer> profil = construireProfil(eleveTrackingId);
        if (profil.isEmpty()) {
            return serieRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), TRI_GENERIQUE))
                    .map(serieMapper::toResponse);
        }
        List<FicheSerie> toutes = serieRepository.findAll();
        List<ScoredFiche<FicheSerie>> scorees = toutes.stream()
                .map(s -> new ScoredFiche<>(scoreMotsCles(s.getTitre(), profil.get().tokens()), s.getCreatedAt(), s))
                .sorted(comparateurScore())
                .collect(Collectors.toList());
        return paginer(scorees, pageable).map(serieMapper::toResponse);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Construction du profil élève
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Profil exploitable si l'élève existe ET a au moins un champ de
     * personnalisation (niveau, filière, métier souhaité, matières).
     * Élève introuvable ou profil vide → {@link Optional#empty()} →
     * repli générique (liste non vide, pas d'erreur).
     */
    private Optional<ProfilExplorer> construireProfil(UUID eleveTrackingId) {
        if (eleveTrackingId == null) {
            return Optional.empty();
        }
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId).orElse(null);
        if (eleve == null) {
            log.info("Explorer personnalisé : élève {} introuvable → repli générique", eleveTrackingId);
            return Optional.empty();
        }
        boolean exploitable = eleve.getNiveau() != null || eleve.getFiliere() != null
                || eleve.getMetierSouhaite() != null || eleve.getMatieresPreferees() != null;
        if (!exploitable) {
            log.info("Explorer personnalisé : profil de l'élève {} incomplet → repli générique", eleveTrackingId);
            return Optional.empty();
        }

        Set<String> tokens = tokensDe(eleve.getFiliere(), eleve.getMetierSouhaite(), eleve.getMatieresPreferees());

        List<TestRIASECResultat> riasecList = riasecRepository
                .findByEleveTrackingIdOrderByDatePassationDesc(eleveTrackingId.toString());
        List<Double> riasec = null;
        if (!riasecList.isEmpty()) {
            TestRIASECResultat t = riasecList.get(0);
            riasec = List.of(
                    (t.getScoreRealiste() != null ? t.getScoreRealiste() : 0) / 10.0,
                    (t.getScoreInvestigateur() != null ? t.getScoreInvestigateur() : 0) / 10.0,
                    (t.getScoreArtistique() != null ? t.getScoreArtistique() : 0) / 10.0,
                    (t.getScoreSocial() != null ? t.getScoreSocial() : 0) / 10.0,
                    (t.getScoreEntreprenant() != null ? t.getScoreEntreprenant() : 0) / 10.0,
                    (t.getScoreConventionnel() != null ? t.getScoreConventionnel() : 0) / 10.0);
        }
        return Optional.of(new ProfilExplorer(riasec, tokens));
    }

    // ─────────────────────────────────────────────────────────────────────
    // Scoring
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Score d'une filière : similarité RIASEC (cosinus avec le catalogue
     * {@link ProfilFiliereRiasecCatalog}, même logique que le moteur
     * 3 signaux) pondérée par le score de mots-clés du profil.
     * Sans RIASEC disponible, le classement repose sur les mots-clés
     * (déterministe, évite de tout mettre à égalité).
     */
    double scoreFiliere(FicheFiliere fiche, ProfilExplorer profil) {
        double motsCles = scoreMotsCles(fiche.getTitre() + " " + (fiche.getDomaine() != null ? fiche.getDomaine() : ""),
                profil.tokens());
        if (profil.riasec() == null) {
            return motsCles;
        }
        double[] catalogue = ProfilFiliereRiasecCatalog.profilPour(fiche.getTitre());
        double similarite = cosinusSimilarite(profil.riasec(), catalogue);
        return 0.6 * similarite + 0.4 * motsCles;
    }

    /**
     * Mots-clés (titre/domaine) contenant au moins un token du profil.
     * Matching par sous-chaîne ET par préfixe (4 premiers caractères) pour
     * traiter les flexions : "maths" matche "Mathématiques" (préfixe "math"),
     * "informatique"/"informaticien" matchent tous deux "Informatique".
     */
    double scoreMotsCles(String texte, Set<String> tokens) {
        if (texte == null || texte.isBlank() || tokens.isEmpty()) {
            return 0.0;
        }
        String normalise = normaliser(texte);
        long matches = tokens.stream()
                .filter(t -> t.length() >= 3 && (normalise.contains(t)
                        || (t.length() >= 4 && normalise.contains(t.substring(0, 4)))))
                .count();
        if (matches == 0) {
            return 0.0;
        }
        return Math.min(1.0, matches / 2.0);
    }

    /** Cosinus entre le vecteur RIASEC de l'élève et celui d'une filière. */
    static double cosinusSimilarite(List<Double> a, double[] b) {
        if (a == null || b == null || a.size() != 6 || b.length != 6) {
            return 0.0;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < 6; i++) {
            dot += a.get(i) * b[i];
            normA += a.get(i) * a.get(i);
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ─────────────────────────────────────────────────────────────────────
    // Utilitaires
    // ─────────────────────────────────────────────────────────────────────

    /** Tokens de recherche : normalisés (minuscules, sans accents), ≥ 3 caractères. */
    Set<String> tokensDe(String... champs) {
        return Arrays.stream(champs)
                .filter(c -> c != null && !c.isBlank())
                .flatMap(c -> Arrays.stream(normaliser(c).split("[^a-z0-9]+")))
                .filter(t -> t.length() >= 3)
                .collect(Collectors.toSet());
    }

    /** minuscules + suppression des accents. */
    static String normaliser(String s) {
        if (s == null) {
            return "";
        }
        String sansAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sansAccents.toLowerCase(Locale.FRENCH);
    }

    /** Comparateur : score DESC puis createdAt DESC (tri stable, ex æquo → plus récent d'abord). */
    private static <T> Comparator<ScoredFiche<T>> comparateurScore() {
        return Comparator
                .comparingDouble((ScoredFiche<T> s) -> s.score())
                .reversed()
                .thenComparing(ScoredFiche::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    /** Tranche paginée d'une liste déjà triée (pagination en mémoire). */
    private static <T> Page<T> paginer(List<ScoredFiche<T>> liste, Pageable pageable) {
        int total = liste.size();
        int from = (int) Math.min(pageable.getOffset(), total);
        int to = (int) Math.min(from + pageable.getPageSize(), total);
        List<T> content = from >= to ? new ArrayList<>() : new ArrayList<>(
                liste.subList(from, to).stream().map(ScoredFiche::fiche).collect(Collectors.toList()));
        return new PageImpl<>(content, pageable, total);
    }

    /** Tuple interne (score, date de création, fiche) pour le tri stable. */
    private record ScoredFiche<T>(double score, java.time.LocalDateTime createdAt, T fiche) {
    }

    /** Profil d'élève extrait pour le scoring (RIASEC optionnel + tokens). */
    record ProfilExplorer(List<Double> riasec, Set<String> tokens) {
    }
}
