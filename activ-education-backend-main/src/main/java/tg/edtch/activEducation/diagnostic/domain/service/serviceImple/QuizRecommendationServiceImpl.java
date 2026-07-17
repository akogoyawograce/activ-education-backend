package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.QuestionMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;
import tg.edtch.activEducation.diagnostic.domain.service.QuizRecommendationService;
import tg.edtch.activEducation.diagnostic.repository.QuestionRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.NoteSaisiManuelRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizRecommendationServiceImpl implements QuizRecommendationService {

    private final EleveRepository eleveRepository;
    private final NoteSaisiManuelRepository noteRepository;
    private final ResultatDiagnosticRepository resultatRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    private static final Map<String, String> MATIERE_TO_DOMAINE = Map.ofEntries(
            Map.entry("mathematiques", "Sciences"),
            Map.entry("physique", "Sciences"),
            Map.entry("chimie", "Sciences"),
            Map.entry("svt", "Sciences"),
            Map.entry("sciences", "Sciences"),
            Map.entry("francais", "Lettres"),
            Map.entry("français", "Lettres"),
            Map.entry("anglais", "Langues"),
            Map.entry("histoire", "Sciences Humaines"),
            Map.entry("geographie", "Sciences Humaines"),
            Map.entry("géographie", "Sciences Humaines"),
            Map.entry("philosophie", "Lettres"),
            Map.entry("eps", "Sport"),
            Map.entry("arts", "Arts"),
            Map.entry("technologie", "Technique"),
            Map.entry("informatique", "Technique"),
            Map.entry("musique", "Arts"),
            Map.entry("ses", "Sciences Humaines"),
            Map.entry("economie", "Commerce"),
            Map.entry("économie", "Commerce"),
            Map.entry("comptabilite", "Administration"),
            Map.entry("comptabilité", "Administration"),
            Map.entry("gestion", "Commerce")
    );

    private static final Map<String, String> RIASEC_TO_DOMAINE = Map.of(
            "R", "Technique",
            "I", "Sciences",
            "A", "Arts",
            "S", "Sciences Humaines",
            "E", "Commerce",
            "C", "Administration"
    );

    private static final Map<String, List<String>> METIER_KEYWORD_TO_DOMAINE = Map.ofEntries(
            Map.entry("medecin", List.of("Sciences")),
            Map.entry("médecin", List.of("Sciences")),
            Map.entry("ingenieur", List.of("Sciences", "Technique")),
            Map.entry("ingénieur", List.of("Sciences", "Technique")),
            Map.entry("avocat", List.of("Lettres", "Sciences Humaines")),
            Map.entry("enseignant", List.of("Sciences Humaines", "Lettres")),
            Map.entry("comptable", List.of("Administration", "Commerce")),
            Map.entry("architecte", List.of("Arts", "Technique")),
            Map.entry("informaticien", List.of("Technique", "Sciences")),
            Map.entry("commercial", List.of("Commerce")),
            Map.entry("manager", List.of("Commerce", "Administration")),
            Map.entry("artiste", List.of("Arts")),
            Map.entry("sportif", List.of("Sport")),
            Map.entry("journaliste", List.of("Lettres", "Langues")),
            Map.entry("pharmacien", List.of("Sciences")),
            Map.entry("infirmier", List.of("Sciences", "Sciences Humaines"))
    );

    @Override
    public List<QuestionResponse> recommanderQuestions(UUID eleveTrackingId, UUID quizTrackingId, int nombreQuestions) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException("Élève introuvable : " + eleveTrackingId));

        List<NoteSaisiManuel> notes = noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId);
        Optional<ResultatDiagnostic> dernierResultat = resultatRepository
                .findFirstByEleveTrackingIdAndQuizTrackingIdOrderByDatePassageDesc(eleveTrackingId, quizTrackingId);

        int niveauBase = calculerNiveauBase(eleve.getTypeApprenant());
        Map<String, Double> poidsDomaines = calculerPoidsDomaines(eleve, notes, dernierResultat.orElse(null));

        List<Question> toutesQuestions = questionRepository.findByQuizTrackingIdOrderByOrdreAsc(quizTrackingId);
        List<Question> filtrees = filtrerQuestions(toutesQuestions, niveauBase, poidsDomaines, nombreQuestions, eleve.getTypeApprenant());

        if (filtrees.size() < nombreQuestions && filtrees.size() < toutesQuestions.size()) {
            Set<UUID> dejaSelectionnees = filtrees.stream().map(Question::getTrackingId).collect(Collectors.toSet());
            List<Question> complement = toutesQuestions.stream()
                    .filter(q -> !dejaSelectionnees.contains(q.getTrackingId()))
                    .filter(q -> q.getNiveauCible() == null || correspondNiveau(q.getNiveauCible(), eleve.getTypeApprenant()))
                    .collect(Collectors.toList());
            Collections.shuffle(complement);
            int besoin = nombreQuestions - filtrees.size();
            filtrees.addAll(complement.stream().limit(besoin).toList());
        }

        if (filtrees.size() > nombreQuestions) {
            filtrees = filtrees.subList(0, nombreQuestions);
        }

        log.info("Recommandation pour élève {} : {} questions sur {} disponibles (quiz {})",
                eleveTrackingId, filtrees.size(), toutesQuestions.size(), quizTrackingId);

        return filtrees.stream().map(questionMapper::toResponse).collect(Collectors.toList());
    }

    int calculerNiveauBase(TypeApprenant type) {
        if (type == null) return 2;
        return switch (type) {
            case ECOLIER -> 1;
            case COLLEGIEN -> 2;
            case LYCEEN -> 3;
            case ETUDIANT -> 4;
            case PROFESSIONNEL -> 4;
            case AUTRE -> 2;
        };
    }

    Map<String, Double> calculerPoidsDomaines(Eleve eleve, List<NoteSaisiManuel> notes, ResultatDiagnostic dernierResultat) {
        Map<String, Double> poids = new HashMap<>();
        List<String> domaines = List.of("Sciences", "Lettres", "Langues", "Arts", "Technique",
                "Sciences Humaines", "Commerce", "Administration", "Sport");
        for (String d : domaines) poids.put(d, 1.0);

        if (notes != null && !notes.isEmpty()) {
            List<NoteSaisiManuel> faibles = notes.stream()
                    .filter(n -> n.getNote() < 10.0)
                    .collect(Collectors.toList());
            if (!faibles.isEmpty()) {
                double poidsRenfort = 1.5;
                for (NoteSaisiManuel n : faibles) {
                    String domaine = matiereVersDomaine(n.getMatiere());
                    if (domaine != null) {
                        poids.merge(domaine, poidsRenfort, Double::sum);
                    }
                }
            }
        }

        if (eleve.getMetierSouhaite() != null && !eleve.getMetierSouhaite().isBlank()) {
            String metier = eleve.getMetierSouhaite().toLowerCase().trim();
            for (Map.Entry<String, List<String>> entry : METIER_KEYWORD_TO_DOMAINE.entrySet()) {
                if (metier.contains(entry.getKey())) {
                    for (String domaine : entry.getValue()) {
                        poids.merge(domaine, 1.5, Double::sum);
                    }
                    break;
                }
            }
        }

        if (eleve.getMatieresPreferees() != null && !eleve.getMatieresPreferees().isBlank()) {
            String[] prefs = eleve.getMatieresPreferees().split(",");
            for (String pref : prefs) {
                String domaine = matiereVersDomaine(pref.trim());
                if (domaine != null) {
                    poids.merge(domaine, 1.2, Double::sum);
                }
            }
        }

        if (dernierResultat != null && dernierResultat.getProfilDecouvert() != null) {
            String profil = dernierResultat.getProfilDecouvert().toUpperCase();
            for (Map.Entry<String, String> entry : RIASEC_TO_DOMAINE.entrySet()) {
                if (profil.contains(entry.getKey())) {
                    poids.merge(entry.getValue(), 1.3, Double::sum);
                }
            }
        }

        return poids;
    }

    List<Question> filtrerQuestions(List<Question> questions, int niveauBase, Map<String, Double> poidsDomaines, int nombre, TypeApprenant typeApprenant) {
        List<Question> eligible = questions.stream()
                .filter(q -> q.getNiveauCible() == null || correspondNiveau(q.getNiveauCible(), typeApprenant))
                .filter(q -> q.getDifficulte() == null || Math.abs(q.getDifficulte() - niveauBase) <= 1)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            eligible = new ArrayList<>(questions);
        }

        List<Question> ponderees = new ArrayList<>();
        Random rand = new Random();
        for (Question q : eligible) {
            String domaine = q.getDomaine();
            double poids = (domaine != null && poidsDomaines.containsKey(domaine))
                    ? poidsDomaines.get(domaine)
                    : 1.0;
            int repetitions = (int) Math.round(poids * 2);
            for (int i = 0; i < repetitions; i++) {
                ponderees.add(q);
            }
        }

        Collections.shuffle(ponderees, rand);
        Set<UUID> vues = new HashSet<>();
        List<Question> selection = new ArrayList<>();
        for (Question q : ponderees) {
            if (selection.size() >= nombre) break;
            if (vues.add(q.getTrackingId())) {
                selection.add(q);
            }
        }

        if (selection.size() < nombre) {
            for (Question q : eligible) {
                if (selection.size() >= nombre) break;
                if (vues.add(q.getTrackingId())) {
                    selection.add(q);
                }
            }
        }

        return selection;
    }

    String matiereVersDomaine(String matiere) {
        if (matiere == null) return null;
        String key = matiere.toLowerCase().trim()
                .replaceAll("\\s+", "")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[ôö]", "o")
                .replaceAll("[îï]", "i")
                .replaceAll("[ç]", "c");
        return MATIERE_TO_DOMAINE.entrySet().stream()
                .filter(e -> key.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    boolean correspondNiveau(String niveauCible, TypeApprenant type) {
        if (niveauCible == null || niveauCible.isBlank()) return true;
        if (type == null) return true;
        String n = niveauCible.toLowerCase();
        return switch (type) {
            case ECOLIER -> n.contains("ecolier") || n.contains("primaire");
            case COLLEGIEN -> n.contains("collegien") || n.contains("collège") || n.contains("college");
            case LYCEEN -> n.contains("lyceen") || n.contains("lycée") || n.contains("lycee") || n.contains("bac");
            case ETUDIANT -> n.contains("etudiant") || n.contains("université") || n.contains("universite") || n.contains("supérieur") || n.contains("superieur");
            case PROFESSIONNEL -> n.contains("professionnel") || n.contains("formation");
            case AUTRE -> true;
        };
    }
}
