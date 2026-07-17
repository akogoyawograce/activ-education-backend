package tg.edtch.activEducation.prediction.application.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.NiveauFiliere;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.domain.repository.NiveauFiliereRepository;
import tg.edtch.activEducation.prediction.application.dto.FiliereScoreeResponse;
import tg.edtch.activEducation.prediction.application.dto.ProfilEleve;
import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;
import tg.edtch.activEducation.prediction.application.service.Recommandation3SignauxService;
import tg.edtch.activEducation.prediction.domain.config.PredictionProperties;
import tg.edtch.activEducation.prediction.domain.entite.EngagementSignal;
import tg.edtch.activEducation.prediction.domain.repository.EngagementSignalRepository;
import tg.edtch.activEducation.prediction.domain.service.NoteTrajectoireService;
import tg.edtch.activEducation.prediction.domain.util.ProfilFiliereRiasecCatalog;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NotesHistorique;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.profil.domain.repository.NotesHistoriqueRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.riasec.domain.entite.TestRIASECResultat;
import tg.edtch.activEducation.riasec.repository.TestRIASECResultatRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du moteur 3 signaux (Phase 3).
 *
 * <p>Stratégie :
 * <ol>
 *   <li>Charger le profil de l'élève (RIASEC + 3 dernières notes + niveau).</li>
 *   <li>Récupérer les filières candidates : celles dont le niveau est dans
 *       la liste de niveaux suivants logiques (pour un élève de Terminale :
 *       BAC_1, BAC_2, BAC_3).</li>
 *   <li>Pour chaque candidate, calculer les 3 sous-scores et le combiné.</li>
 *   <li>Forcer l'inclusion de 1-2 "découvertes" : filières à fort
 *       score_aspiration et/ou score_realite mais faible engagement.</li>
 *   <li>Retourner le top N trié par score_final DESC.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class Recommandation3SignauxServiceImpl implements Recommandation3SignauxService {

    private final EleveRepository eleveRepository;
    private final TestRIASECResultatRepository riasecRepository;
    private final NotesHistoriqueRepository notesRepository;
    private final NiveauFiliereRepository niveauFiliereRepository;
    private final FicheFiliereRepository ficheFiliereRepository;
    private final EngagementSignalRepository engagementRepository;
    private final NoteTrajectoireService trajectoireService;
    private final PredictionProperties properties;

    @Override
    public Recommandation3SignauxResponse recommander(UUID eleveTrackingId) {
        // 1) Profil élève
        ProfilEleve profil = construireProfilEleve(eleveTrackingId);
        log.debug("Profil élève {} : niveau={}, riasec={}, noteActuelle={}, confiance={}",
                eleveTrackingId, profil.getNiveau(), profil.getRiasec(),
                profil.getNoteActuelle(), profil.getConfianceTrajectoire());

        // 2) Filières candidates (niveau ≥ au niveau de l'élève)
        List<FicheFiliere> candidates = candidatsPourProfil(profil);

        if (candidates.isEmpty()) {
            log.warn("Aucune filière candidate pour l'élève {}", eleveTrackingId);
            return Recommandation3SignauxResponse.builder()
                    .eleveTrackingId(eleveTrackingId)
                    .top(List.of())
                    .poidsAspiration(properties.getPoidsAspiration())
                    .poidsRealite(properties.getPoidsRealite())
                    .poidsEngagement(properties.poidsEngagementEffectif())
                    .decouvertesAjoutees(0)
                    .profil(profil)
                    .build();
        }

        // 3) Engagement : index par ficheId pour éviter N+1
        Map<Long, EngagementSignal> engagementParFiche = indexerEngagement(
                eleveTrackingId, candidates);

        // 4) Scoring de toutes les candidates
        List<FiliereScoreeResponse> scorees = new ArrayList<>(candidates.size());
        for (FicheFiliere fiche : candidates) {
            BigDecimal aspiration = calculerAspiration(profil, fiche);
            BigDecimal realite = calculerRealite(profil, fiche);
            BigDecimal engagement = calculerEngagement(engagementParFiche.get(fiche.getId()));
            BigDecimal finalScore = combiner(aspiration, realite, engagement);

            scorees.add(FiliereScoreeResponse.builder()
                    .trackingId(fiche.getTrackingId())
                    .titre(fiche.getTitre())
                    .domaine(fiche.getDomaine())
                    .duree(fiche.getDuree())
                    .scoreAspiration(aspiration)
                    .scoreRealite(realite)
                    .scoreEngagement(engagement)
                    .scoreFinal(finalScore)
                    .estDecouverte(false)
                    .raisonClassement(genererRaison(aspiration, realite, engagement))
                    .build());
        }

        // 5) Top N
        scorees.sort(Comparator.comparing(FiliereScoreeResponse::getScoreFinal,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // 6) Découvertes : fort score_aspiration/realite, faible engagement
        //    On pioche hors du top N.
        int decouvertesAjoutees = 0;
        if (properties.getDecouvertesMin() > 0) {
            List<FiliereScoreeResponse> decouvertes = selectionnerDecouvertes(
                    scorees,
                    properties.getTopN(),
                    properties.getDecouvertesMin());

            for (FiliereScoreeResponse d : decouvertes) {
                d.setEstDecouverte(true);
                d.setRaisonClassement("Découverte : fort potentiel, peu consultée");
            }
            decouvertesAjoutees = decouvertes.size();
        }

        List<FiliereScoreeResponse> top = scorees.stream()
                .limit(properties.getTopN())
                .collect(Collectors.toList());

        return Recommandation3SignauxResponse.builder()
                .eleveTrackingId(eleveTrackingId)
                .top(top)
                .poidsAspiration(properties.getPoidsAspiration())
                .poidsRealite(properties.getPoidsRealite())
                .poidsEngagement(properties.poidsEngagementEffectif())
                .decouvertesAjoutees(decouvertesAjoutees)
                .profil(profil)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Construction du profil élève
    // ─────────────────────────────────────────────────────────────────────

    private ProfilEleve construireProfilEleve(UUID eleveTrackingId) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));

        // RIASEC : dernier test
        Optional<TestRIASECResultat> dernierRiasec = riasecRepository
                .findByEleveTrackingIdOrderByDatePassationDesc(eleveTrackingId.toString())
                .stream().findFirst();

        List<Double> riasec6;
        String profilDecouvert = null;
        if (dernierRiasec.isPresent()) {
            TestRIASECResultat t = dernierRiasec.get();
            riasec6 = List.of(
                    t.getScoreRealiste() / 10.0,
                    t.getScoreInvestigateur() / 10.0,
                    t.getScoreArtistique() / 10.0,
                    t.getScoreSocial() / 10.0,
                    t.getScoreEntreprenant() / 10.0,
                    t.getScoreConventionnel() / 10.0
            );
            profilDecouvert = t.getCodeProfil();
        } else {
            riasec6 = List.of(0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        }

        // Notes : 3 dernières moyennes générales (ordre DESC → on inverse)
        List<NotesHistorique> notes = notesRepository
                .findByEleveIdAndEstMoyenneGeneraleTrueOrderByAnneeScolaireDesc(eleve.getId());
        List<BigDecimal> notesCroissant = notes.stream()
                .sorted(Comparator.comparing(NotesHistorique::getAnneeScolaire))
                .map(NotesHistorique::getMoyenne)
                .collect(Collectors.toList());

        NoteTrajectoireService.Trajectoire traj = trajectoireService.calculer(notesCroissant);

        return ProfilEleve.builder()
                .eleveId(eleve.getId())
                .trackingId(eleve.getTrackingId())
                .niveau(eleve.getNiveau() != null ? eleve.getNiveau().name() : null)
                .profilDecouvert(profilDecouvert)
                .riasec(riasec6)
                .noteActuelle(traj.noteActuelle())
                .noteExtrapolée(traj.noteExtrapolée())
                .pente(traj.pente())
                .confianceTrajectoire(traj.confiance())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Candidats (filières dont le niveau est >= au niveau de l'élève)
    // ─────────────────────────────────────────────────────────────────────

    private List<FicheFiliere> candidatsPourProfil(ProfilEleve profil) {
        NiveauScolaire niveauActuel = profil.getNiveau() != null
                ? NiveauScolaire.parse(profil.getNiveau())
                : null;
        if (niveauActuel == null) {
            // Pas de niveau : on prend tout (mode dégradé)
            return ficheFiliereRepository.findAll();
        }

        Set<NiveauScolaire> niveauxCibles = niveauxSuivants(niveauActuel);
        Set<Long> ficheIds = new HashSet<>();
        for (NiveauScolaire n : niveauxCibles) {
            niveauFiliereRepository.findByNiveau(n)
                    .forEach(m -> ficheIds.add(m.getFicheFiliere().getId()));
        }
        if (ficheIds.isEmpty()) {
            return List.of();
        }
        return ficheFiliereRepository.findAllById(ficheIds);
    }

    /**
     * Renvoie l'ensemble des niveaux où un élève du niveau {@code actuel}
     * peut entrer (lui-même et tous les niveaux supérieurs).
     */
    private static Set<NiveauScolaire> niveauxSuivants(NiveauScolaire actuel) {
        Set<NiveauScolaire> res = new HashSet<>();
        boolean vu = false;
        for (NiveauScolaire n : NiveauScolaire.values()) {
            if (n == actuel) vu = true;
            if (vu) res.add(n);
        }
        // Inclure aussi le niveau actuel
        res.add(actuel);
        return res;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Engagement
    // ─────────────────────────────────────────────────────────────────────

    private Map<Long, EngagementSignal> indexerEngagement(UUID eleveTrackingId,
                                                          List<FicheFiliere> fiches) {
        List<EngagementSignal> signaux = engagementRepository
                .findByEleveIdAndFicheType(eleveRepository.findByTrackingId(eleveTrackingId)
                        .map(Eleve::getId).orElse(-1L), EngagementSignal.TypeFiche.FILIERE);
        return signaux.stream()
                .filter(s -> fiches.stream().anyMatch(f -> f.getId().equals(s.getFicheId())))
                .collect(Collectors.toMap(EngagementSignal::getFicheId, s -> s, (a, b) -> a));
    }

    private BigDecimal calculerEngagement(EngagementSignal signal) {
        if (signal == null) {
            return BigDecimal.ZERO;
        }
        // Formule : 0.1·consultations (cap 5) + 0.5·enFavori + 0.3·similaire_recherche
        double consultations = Math.min(5, signal.getNbConsultations() != null
                ? signal.getNbConsultations() : 0);
        double enFavori = Boolean.TRUE.equals(signal.getEnFavori()) ? 1.0 : 0.0;
        double similaire = signal.getScoreSimilariteRecherche() != null
                ? signal.getScoreSimilariteRecherche().doubleValue() : 0.0;
        double raw = 0.1 * consultations + 0.5 * enFavori + 0.3 * similaire;
        double sature = 1.0 - Math.exp(-raw); // sigmoid-like
        return BigDecimal.valueOf(Math.min(1.0, Math.max(0.0, sature)))
                .setScale(3, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Aspiration (cosinus RIASEC)
    // ─────────────────────────────────────────────────────────────────────

    BigDecimal calculerAspiration(ProfilEleve profil, FicheFiliere fiche) {
        double[] profilFiliere = ProfilFiliereRiasecCatalog.profilPour(fiche.getTitre());
        return cosinusSimilitude(profil.getRiasec(), profilFiliere);
    }

    /**
     * Similarité cosinus entre deux vecteurs.
     * Si l'un des vecteurs est nul, retourne 0.
     */
    static BigDecimal cosinusSimilitude(List<Double> a, double[] b) {
        if (a == null || b == null || a.size() != 6) {
            return BigDecimal.ZERO;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < 6; i++) {
            dot += a.get(i) * b[i];
            normA += a.get(i) * a.get(i);
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return BigDecimal.ZERO;
        }
        double sim = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, sim)))
                .setScale(3, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Réalité (notes vs seuil d'admission)
    // ─────────────────────────────────────────────────────────────────────

    BigDecimal calculerRealite(ProfilEleve profil, FicheFiliere fiche) {
        // On prend la note extrapolée (plus optimiste mais prédictive)
        // plutôt que la note actuelle brute. Si pas de note, 0.5 neutre.
        BigDecimal note = profil.getNoteExtrapolée() != null
                ? profil.getNoteExtrapolée()
                : profil.getNoteActuelle();
        if (note == null) {
            return new BigDecimal("0.500");
        }
        BigDecimal seuil = properties.getSeuilAdmissionDefaut();
        // ratio = note / seuil, borné à 1.0
        // + bonus de 0.05 si tendance positive (récompense le progrès)
        BigDecimal ratio = note.divide(seuil, 3, RoundingMode.HALF_UP);
        double tendanceBonus = profil.getPente() != null && profil.getPente().doubleValue() > 0
                ? 0.05 : 0.0;
        double score = Math.min(1.0, ratio.doubleValue() + tendanceBonus);
        return BigDecimal.valueOf(Math.max(0.0, score)).setScale(3, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Combinaison pondérée
    // ─────────────────────────────────────────────────────────────────────

    BigDecimal combiner(BigDecimal aspiration, BigDecimal realite, BigDecimal engagement) {
        BigDecimal pa = properties.getPoidsAspiration();
        BigDecimal pr = properties.getPoidsRealite();
        BigDecimal pe = properties.poidsEngagementEffectif(); // plafond appliqué
        BigDecimal s = aspiration.multiply(pa)
                .add(realite.multiply(pr))
                .add(engagement.multiply(pe));
        return s.setScale(3, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Découvertes
    // ─────────────────────────────────────────────────────────────────────

    private List<FiliereScoreeResponse> selectionnerDecouvertes(
            List<FiliereScoreeResponse> scorees,
            int topN,
            int nb) {
        if (scorees.size() <= topN) {
            return List.of();
        }
        // Hors top N, on prend celles avec score_aspiration >= 0.6 ET
        // score_engagement <= 0.1 (= peu consultées mais bon profil)
        return scorees.stream()
                .skip(topN)
                .filter(s -> s.getScoreAspiration() != null
                          && s.getScoreAspiration().doubleValue() >= 0.6)
                .filter(s -> s.getScoreEngagement() != null
                          && s.getScoreEngagement().doubleValue() <= 0.1)
                .limit(nb)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Explication textuelle
    // ─────────────────────────────────────────────────────────────────────

    private String genererRaison(BigDecimal aspiration, BigDecimal realite, BigDecimal engagement) {
        StringBuilder sb = new StringBuilder();
        if (aspiration != null && aspiration.doubleValue() >= 0.7) sb.append("Profil RIASEC très aligné. ");
        else if (aspiration != null && aspiration.doubleValue() >= 0.5) sb.append("Profil RIASEC compatible. ");
        if (realite != null && realite.doubleValue() >= 0.8) sb.append("Notes au-dessus du seuil. ");
        else if (realite != null && realite.doubleValue() >= 0.6) sb.append("Notes correctes vs admission. ");
        if (engagement != null && engagement.doubleValue() >= 0.5) sb.append("Déjà consulté/intéressé. ");
        if (sb.length() == 0) sb.append("Score pondéré des 3 signaux.");
        return sb.toString().trim();
    }
}
