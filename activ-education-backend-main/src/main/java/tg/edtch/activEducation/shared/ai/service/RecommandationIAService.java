package tg.edtch.activEducation.shared.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.NoteSaisiManuelRepository;
import tg.edtch.activEducation.shared.ai.domain.dto.RecommandationIAResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommandationIAService {

    private static final Set<String> STOP_WORDS = Set.of(
            "je", "veux", "devenir", "suis", "j", "aime", "aimerais", "passionne", "passionnee",
            "un", "une", "le", "la", "les", "des", "du", "de", "et", "en", "au", "aux",
            "pour", "dans", "avec", "que", "qui", "ce", "cette", "mon", "ma", "mes",
            "domaine", "metier", "filiere", "etudier", "apprendre", "faire");

    private static final int MAX_ENTREES = 50;
    private static final int MAX_SELECTION = 10;
    private static final int MAX_OFFRE_FORMATION = 160;

    private final EleveRepository eleveRepository;
    private final NoteSaisiManuelRepository noteRepository;
    private final ResultatDiagnosticRepository resultatRepository;
    private final QuizRepository quizRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheMetierRepository metierRepository;
    private final FicheEtablissementRepository etablissementRepository;
    private final AIEmbeddingService aiService;

    /**
     * Construit la recommandation IA d'un élève.
     *
     * <p>Retourne désormais un {@link RecommandationIAResponse} typé pour
     * distinguer 3 cas (cf. JOURNAL_BORD_IA.md, 6 août 2026) :
     * <ul>
     *   <li>{@code OK} : recommandation générée par le LLM</li>
     *   <li>{@code PROFIL_INCOMPLET} : profil trop vide pour faire une
     *       recommandation pertinente, message déterministe invitant à
     *       compléter le profil</li>
     *   <li>{@code ERREUR_LLM} : le LLM (OpenAI, Ollama, …) a échoué
     *       malgré un profil complet, message invitant à réessayer</li>
     * </ul>
     * </p>
     */
    public RecommandationIAResponse genererRecommandation(UUID eleveTrackingId) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new RuntimeException("Élève introuvable"));

        boolean profilRempli = false;
        StringBuilder profilBuilder = new StringBuilder();
        profilBuilder.append("Profil de l'élève :\n");
        profilBuilder.append("- Niveau : ").append(eleve.getNiveau() != null ? eleve.getNiveau() : "Non renseigné").append("\n");
        profilBuilder.append("- Type : ").append(eleve.getTypeApprenant() != null ? eleve.getTypeApprenant().name() : "Non renseigné").append("\n");
        profilBuilder.append("- Établissement : ").append(eleve.getEtablissement() != null ? eleve.getEtablissement() : "Non renseigné").append("\n");
        profilBuilder.append("- Filière actuelle : ").append(eleve.getFiliere() != null ? eleve.getFiliere() : "Non renseigné").append("\n");
        profilBuilder.append("- Métier souhaité : ").append(eleve.getMetierSouhaite() != null ? eleve.getMetierSouhaite() : "Non renseigné").append("\n");
        profilBuilder.append("- Matières préférées : ").append(eleve.getMatieresPreferees() != null ? eleve.getMatieresPreferees() : "Non renseigné").append("\n");

        boolean aDesNotes = false;
        List<NoteSaisiManuel> notes = noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId);
        if (!notes.isEmpty()) {
            aDesNotes = true;
            profilBuilder.append("\nNotes scolaires :\n");
            for (NoteSaisiManuel note : notes) {
                profilBuilder.append("- ").append(note.getMatiere()).append(" : ").append(note.getNote()).append("/20\n");
            }
        }

        boolean aDesQuiz = false;
        List<ResultatDiagnostic> resultats = resultatRepository
                .findByEleveTrackingIdOrderByDatePassageDesc(eleveTrackingId,
                        PageRequest.of(0, 5))
                .getContent();
        if (!resultats.isEmpty()) {
            aDesQuiz = true;
            profilBuilder.append("\nRésultats de quiz d'orientation :\n");
            for (ResultatDiagnostic r : resultats) {
                Optional<Quiz> quiz = quizRepository.findByTrackingId(r.getQuiz().getTrackingId());
                String nomQuiz = quiz.map(Quiz::getTitre).orElse("Quiz inconnu");
                profilBuilder.append("- ").append(nomQuiz).append(" : score ").append(r.getScoreFinal()).append("\n");
            }
        }

        profilRempli = eleve.getNiveau() != null || eleve.getFiliere() != null
                || eleve.getMetierSouhaite() != null || eleve.getMatieresPreferees() != null
                || aDesNotes || aDesQuiz;

        if (!profilRempli) {
            // Cas "profil incomplet" : message déterministe, AUCUN appel LLM.
            // Le frontend affichera un bandeau bleu "complète ton profil" plutôt
            // qu'un message d'erreur générique.
            log.info("Recommandation IA : profil incomplet pour élève {} "
                            + "(niveau={}, filiere={}, metier={}, matieres={}, notes={}, quiz={}) — "
                            + "retour PROFIL_INCOMPLET sans appel LLM",
                    eleveTrackingId,
                    eleve.getNiveau(), eleve.getFiliere(), eleve.getMetierSouhaite(),
                    eleve.getMatieresPreferees(), aDesNotes, aDesQuiz);
            return RecommandationIAResponse.profilIncomplet(
                "Je n'ai pas encore assez d'informations sur toi pour te faire une recommandation personnalisée. "
                + "Pour obtenir ta recommandation, je te propose de :\n\n"
                + "1️⃣ Compléter ton profil (niveau, filière, métier souhaité)\n"
                + "2️⃣ Renseigner tes notes scolaires\n"
                + "3️⃣ Passer les quiz d'orientation (RIASEC, personnalité)\n\n"
                + "Une fois ces informations remplies, reviens ici et je pourrai te générer "
                + "une recommandation sur mesure adaptée à ton profil ! 🎯");
        }

        // Mots-clés extraits du profil pour filtrer les fiches pertinentes.
        List<String> motsCles = extraireMotsCles(eleve);
        log.info("Recommandation IA : mots-clés extraits du profil = {}", motsCles);

        List<FicheFiliere> filieres = selectionnerFilieres(motsCles);
        List<FicheMetier> metiers = selectionnerMetiers(motsCles);
        List<FicheEtablissement> etablissements = selectionnerEtablissements(filieres, motsCles);

        StringBuilder contexteBuilder = new StringBuilder();
        contexteBuilder.append("\nFilières disponibles au Togo (liste exhaustive, NE PAS en inventer) :\n");
        for (FicheFiliere f : filieres) {
            contexteBuilder.append("- ").append(f.getTitre());
            if (f.getDomaine() != null && !f.getDomaine().isBlank()) {
                contexteBuilder.append(" [domaine : ").append(f.getDomaine()).append("]");
            }
            contexteBuilder.append(" : ").append(f.getResume() != null ? f.getResume() : "");
            if (f.getDebouchesMetiers() != null && !f.getDebouchesMetiers().isBlank()) {
                contexteBuilder.append(" (débouchés : ").append(f.getDebouchesMetiers()).append(")");
            }
            contexteBuilder.append("\n");
        }
        contexteBuilder.append("\nMétiers disponibles au Togo (liste exhaustive, NE PAS en inventer) :\n");
        for (FicheMetier m : metiers) {
            contexteBuilder.append("- ").append(m.getTitre());
            if (m.getSecteur() != null && !m.getSecteur().isBlank()) {
                contexteBuilder.append(" [secteur : ").append(m.getSecteur()).append("]");
            }
            contexteBuilder.append(" : ").append(m.getResume() != null ? m.getResume() : "");
            contexteBuilder.append("\n");
        }
        contexteBuilder.append("\nÉtablissements au Togo (liste exhaustive, NE PAS en inventer) :\n");
        for (FicheEtablissement e : etablissements) {
            contexteBuilder.append("- ").append(e.getTitre()).append(" (").append(e.getVille()).append(")")
                    .append(" — type : ").append(e.getTypeEtablissement() != null ? e.getTypeEtablissement().name() : "inconnu")
                    .append(", niveau : ").append(e.getNiveau() != null ? e.getNiveau() : "non renseigné");
            if (e.getOffreFormation() != null && !e.getOffreFormation().isBlank()) {
                contexteBuilder.append(", offre : ")
                        .append(abreger(e.getOffreFormation(), MAX_OFFRE_FORMATION));
            }
            contexteBuilder.append("\n");
        }

        String question = "Tu es un conseiller d'orientation qui s'adresse DIRECTEMENT à l'élève (tutoiement, "
                + "jamais « votre élève » ni « l'élève »). À partir de son profil et UNIQUEMENT des listes "
                + "de filières, métiers et établissements fournies :\n"
                + "1. Propose 3 filières d'études adaptées à son profil, en citant uniquement des filières de la liste.\n"
                + "2. Pour chaque filière, propose 1 à 2 métiers correspondants, uniquement parmi la liste.\n"
                + "3. Pour chaque filière, indique l'établissement où l'étudier au Togo, UNIQUEMENT parmi la liste, "
                + "en précisant sa ville et son type (université, lycée, …). Si aucun établissement de la liste ne "
                + "propose cette filière, dis-le explicitement au lieu d'en inventer un.\n"
                + "4. Justifie brièvement chaque choix en t'appuyant sur son profil (niveau, série, métier souhaité, "
                + "notes, quiz).\n"
                + "RÈGLES STRICTES :\n"
                + "- N'invente JAMAIS un établissement, une filière ou un métier absent des listes.\n"
                + "- Ne recommande pas de matières scolaires à étudier (pas de conseil de type « étudie la physique »).\n"
                + "- Ne mélange pas les domaines : si l'élève veut l'informatique, ne recommande pas le médical.\n"
                + "- Pas de répétitions, pas de listes vides. Sois concret, encourageant, en français.\n"
                + "- Termine par un court paragraphe « En conclusion » qui résume le plan d'orientation proposé.";

        try {
            log.info("Recommandation IA : appel LLM pour élève {} (contexte : {} filières, {} métiers, {} établissements publiés)",
                    eleveTrackingId, filieres.size(), metiers.size(), etablissements.size());
            String reponse = aiService.generateAnswer(question, List.of(
                    profilBuilder.toString(),
                    contexteBuilder.toString()));
            log.info("Recommandation IA : LLM OK pour élève {} ({} caractères)",
                    eleveTrackingId, reponse.length());
            return RecommandationIAResponse.ok(reponse);
        } catch (Exception e) {
            // Cas "erreur LLM" : on a essayé d'appeler le LLM, il a échoué.
            // On distingue maintenant ce cas de "profil incomplet" pour que
            // le frontend affiche un bandeau "service indisponible, réessayez"
            // plutôt qu'un message générique confus.
            log.error("Erreur génération recommandation IA pour élève {}", eleveTrackingId, e);
            return RecommandationIAResponse.erreurLlm(
                "Le service de recommandation IA est momentanément indisponible. "
                + "Réessaie dans quelques instants, ou complète ton profil pour "
                + "améliorer la qualité des prochaines recommandations.");
        }
    }

    /** Extrait les mots-clés significatifs du profil (métier souhaité, filière, matières). */
    private List<String> extraireMotsCles(Eleve eleve) {
        Set<String> mots = new HashSet<>();
        for (String champ : List.of(eleve.getMetierSouhaite(), eleve.getFiliere(), eleve.getMatieresPreferees())) {
            if (champ == null || champ.isBlank()) continue;
            for (String mot : champ.toLowerCase(Locale.FRENCH).split("[^a-zà-ÿ0-9]+")) {
                if (mot.length() >= 3 && !STOP_WORDS.contains(mot)) {
                    mots.add(mot);
                }
            }
        }
        return new ArrayList<>(mots);
    }

    /** Filières publiées dont titre/domaine/débouchés correspondent aux mots-clés (repli : 10 premières). */
    private List<FicheFiliere> selectionnerFilieres(List<String> motsCles) {
        List<FicheFiliere> toutes = filiereRepository
                .findAllByEstPublieTrue(PageRequest.of(0, MAX_ENTREES)).getContent();
        List<FicheFiliere> pertinentes = toutes.stream()
                .filter(f -> correspond(f.getTitre(), f.getDomaine(), f.getDebouchesMetiers(), motsCles))
                .limit(MAX_SELECTION)
                .collect(Collectors.toList());
        if (pertinentes.isEmpty() || motsCles.isEmpty()) {
            return toutes.stream().limit(MAX_SELECTION).collect(Collectors.toList());
        }
        return pertinentes;
    }

    /** Métiers publiés dont titre/secteur/missions correspondent aux mots-clés (repli : 10 premiers). */
    private List<FicheMetier> selectionnerMetiers(List<String> motsCles) {
        List<FicheMetier> tous = metierRepository
                .findAllByEstPublieTrue(PageRequest.of(0, MAX_ENTREES)).getContent();
        List<FicheMetier> pertinents = tous.stream()
                .filter(m -> correspond(m.getTitre(), m.getSecteur(), m.getMissions(), motsCles))
                .limit(MAX_SELECTION)
                .collect(Collectors.toList());
        if (pertinents.isEmpty() || motsCles.isEmpty()) {
            return tous.stream().limit(MAX_SELECTION).collect(Collectors.toList());
        }
        return pertinents;
    }

    /**
     * Établissements publiés : priorité à ceux dont les filières proposées
     * recoupent les filières sélectionnées, avec le type/niveau/offre
     * (permet au LLM de ne pas confondre lycée et université).
     */
    private List<FicheEtablissement> selectionnerEtablissements(List<FicheFiliere> filieres, List<String> motsCles) {
        List<FicheEtablissement> tous = etablissementRepository
                .findAllByEstPublieTrue(PageRequest.of(0, MAX_ENTREES)).getContent();
        Set<Long> filiereIds = filieres.stream()
                .map(FicheFiliere::getId)
                .collect(Collectors.toSet());

        List<FicheEtablissement> pertinents = tous.stream()
                .filter(e -> e.getFilieresProposees() != null
                        && e.getFilieresProposees().stream().anyMatch(f -> filiereIds.contains(f.getId())))
                .limit(MAX_SELECTION)
                .collect(Collectors.toList());
        if (!pertinents.isEmpty()) {
            return pertinents;
        }
        // Repli 1 : correspondance texte (titre/offre) sur les mots-clés.
        List<FicheEtablissement> texte = tous.stream()
                .filter(e -> correspond(e.getTitre(), e.getOffreFormation(), null, motsCles))
                .limit(MAX_SELECTION)
                .collect(Collectors.toList());
        if (!texte.isEmpty()) {
            return texte;
        }
        // Repli 2 : 10 premiers publiés.
        return tous.stream().limit(MAX_SELECTION).collect(Collectors.toList());
    }

    /** Vrai si l'un des champs (non null) contient l'un des mots-clés. */
    private boolean correspond(String a, String b, String c, List<String> motsCles) {
        if (motsCles.isEmpty()) return true;
        StringBuilder sb = new StringBuilder();
        for (String champ : new String[]{a, b, c}) {
            if (champ != null) sb.append(champ).append(' ');
        }
        String texte = sb.toString().toLowerCase(Locale.FRENCH);
        return motsCles.stream().anyMatch(texte::contains);
    }

    private String abreger(String texte, int max) {
        if (texte.length() <= max) return texte;
        return texte.substring(0, max).trim() + "…";
    }
}