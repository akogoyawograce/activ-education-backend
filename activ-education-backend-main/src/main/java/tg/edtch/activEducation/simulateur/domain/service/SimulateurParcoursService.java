package tg.edtch.activEducation.simulateur.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.*;
import tg.edtch.activEducation.bibliotheque.repository.*;
import tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission;
import tg.edtch.activEducation.diagnostic.repository.SeuilAdmissionRepository;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioResult;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SimulateurParcoursService {

    private final FicheSerieRepository serieRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheMetierRepository metierRepository;
    private final FicheEtablissementRepository etablissementRepository;
    private final SeuilAdmissionRepository seuilRepository;

    public ScenarioResult explorer(ScenarioRequest request) {
        String serieTrackingId = request.getSerieTrackingId();
        Map<String, Double> notesSimulees = request.getNotesSimulees() != null
                ? request.getNotesSimulees()
                : Collections.emptyMap();

        FicheSerie serie = serieTrackingId != null && !serieTrackingId.isBlank()
                ? serieRepository.findByTrackingId(UUID.fromString(serieTrackingId)).orElse(null)
                : null;

        List<FicheFiliere> filieresPossibles = trouverFilieres(serie, request.getMotCleMetier());
        List<SeuilAdmission> tousSeuils = seuilRepository.findAll();

        List<ScenarioResult.FiliereMatch> filieresMatch = new ArrayList<>();
        Set<String> metiersTrackingIds = new HashSet<>();
        Set<String> etablissementsTrackingIds = new HashSet<>();

        for (FicheFiliere filiere : filieresPossibles) {
            List<SeuilAdmission> seuilsFiliere = filtrerSeuils(tousSeuils, filiere);
            int seuilsTotal = seuilsFiliere.size();
            int seuilsValides = compterSeuilsValides(seuilsFiliere, notesSimulees);

            boolean accesNiveau = verifierNiveauAcces(request.getNiveau(), serie, filiere.getNiveauRequis());
            if (!accesNiveau && seuilsTotal == 0) continue;

            double score = calculerScoreCompatibilite(seuilsValides, seuilsTotal, notesSimulees.size(), accesNiveau);

            if (seuilsTotal > 0 && seuilsValides == 0) continue;
            if (score < 10.0) continue;

            ScenarioResult.FiliereMatch match = new ScenarioResult.FiliereMatch();
            match.setTrackingId(filiere.getTrackingId().toString());
            match.setTitre(filiere.getTitre());
            match.setResume(filiere.getResume());
            match.setDomaine(filiere.getDomaine());
            match.setDuree(filiere.getDuree());
            match.setNiveauRequis(filiere.getNiveauRequis());
            match.setScoreCompatibilite(Math.round(score * 10.0) / 10.0);
            match.setSeuilsValides(seuilsValides);
            match.setSeuilsTotal(seuilsTotal);
            filieresMatch.add(match);

            for (FicheMetier metier : filiere.getMetiersPrepares()) {
                metiersTrackingIds.add(metier.getTrackingId().toString());
            }
            for (FicheEtablissement etab : filiere.getEtablissements()) {
                etablissementsTrackingIds.add(etab.getTrackingId().toString());
            }
        }

        filieresMatch.sort(Comparator.comparingDouble(ScenarioResult.FiliereMatch::getScoreCompatibilite).reversed());

        List<ScenarioResult.MetierCible> metiers = chargerMetiers(
                metiersTrackingIds, filieresPossibles, request.getMotCleMetier());
        List<ScenarioResult.EtablissementCible> etablissements = chargerEtablissements(
                etablissementsTrackingIds, filieresPossibles, request.getVille(), request.getTypeEtablissement(),
                request.getEtablissementPublic());

        ScenarioResult.StatsRecap stats = new ScenarioResult.StatsRecap();
        stats.setTotalFilieres(filieresMatch.size());
        stats.setTotalMetiers(metiers.size());
        stats.setTotalEtablissements(etablissements.size());
        stats.setScoreMoyenCompatibilite(filieresMatch.isEmpty() ? 0
                : Math.round(filieresMatch.stream().mapToDouble(ScenarioResult.FiliereMatch::getScoreCompatibilite).average().orElse(0) * 10.0) / 10.0);
        stats.setDureeMin(calculerDureeMin(filieresMatch));
        stats.setDureeMax(calculerDureeMax(filieresMatch));

        ScenarioResult result = new ScenarioResult();
        result.setTitre(request.getTitre() != null ? request.getTitre() : "Mon scénario");
        result.setSerieTitre(serie != null ? serie.getTitre() : "Toutes les séries");
        result.setFilieres(filieresMatch);
        result.setMetiers(metiers);
        result.setEtablissements(etablissements);
        result.setStats(stats);

        log.info("Simulateur : {} → {} filières, {} métiers, {} établissements",
                result.getTitre(), filieresMatch.size(), metiers.size(), etablissements.size());
        return result;
    }

    public List<ScenarioResult> comparer(List<ScenarioRequest> scenarios) {
        List<ScenarioResult> resultats = scenarios.stream()
                .map(this::explorer)
                .collect(Collectors.toList());

        if (resultats.size() < 2) {
            return resultats;
        }

        // Calcul de l'analyse comparative (Chantier A).
        // On attache l'analyse au PREMIER résultat : le front lit la liste
        // et n'a besoin de l'analyse qu'une fois (pas dupliquée N fois).
        ScenarioResult.ComparaisonAnalyse analyse = calculerComparaison(scenarios, resultats);
        resultats.get(0).setComparaison(analyse);
        log.info("Comparaison : {} scénarios, meilleur='{}', pire='{}', {} filière(s) commune(s)",
                resultats.size(), analyse.getMeilleurScenario(),
                analyse.getPireScenario(), analyse.getNombreFilieresCommunes());
        return resultats;
    }

    /**
     * Calcule l'analyse comparative entre N scénarios :
     * <ul>
     *   <li>Le scénario avec le meilleur score moyen de compatibilité</li>
     *   <li>Le scénario avec le pire score moyen</li>
     *   <li>Pour chaque filière présente dans ≥ 2 scénarios : le delta
     *       max-min sur le score de compatibilité</li>
     *   <li>Une synthèse en langage naturel</li>
     * </ul>
     */
    private ScenarioResult.ComparaisonAnalyse calculerComparaison(
            List<ScenarioRequest> requests, List<ScenarioResult> resultats) {

        ScenarioResult.ComparaisonAnalyse analyse = new ScenarioResult.ComparaisonAnalyse();
        analyse.setNombreScenarios(resultats.size());

        // 1) Identifier le meilleur / pire scénario (par score moyen).
        //    En cas d'égalité stricte, on garde la première occurrence
        //    (stable) pour ne pas avoir meilleur=pire=A et pire=A dans
        //    le même set.
        ScenarioResult meilleur = null;
        ScenarioResult pire = null;
        for (ScenarioResult r : resultats) {
            double scoreMoyen = r.getStats() != null ? r.getStats().getScoreMoyenCompatibilite() : 0;
            if (meilleur == null || scoreMoyen > meilleur.getStats().getScoreMoyenCompatibilite()) {
                meilleur = r;
            }
            if (pire == null || scoreMoyen < pire.getStats().getScoreMoyenCompatibilite()) {
                pire = r;
            }
        }
        analyse.setMeilleurScenario(meilleur != null ? meilleur.getTitre() : null);
        analyse.setPireScenario(pire != null ? pire.getTitre() : null);
        analyse.setScoreMoyenMax(meilleur != null && meilleur.getStats() != null
                ? meilleur.getStats().getScoreMoyenCompatibilite() : 0);
        analyse.setScoreMoyenMin(pire != null && pire.getStats() != null
                ? pire.getStats().getScoreMoyenCompatibilite() : 0);

        // 2) Construire l'index filière → scores par scénario
        //    Clé : titre de la filière (normalisé lowercase)
        //    Valeur : Map<scenarioTitre, score>
        Map<String, Map<String, Double>> scoresParFiliere = new java.util.HashMap<>();
        for (ScenarioResult r : resultats) {
            if (r.getFilieres() == null) continue;
            for (ScenarioResult.FiliereMatch f : r.getFilieres()) {
                String key = f.getTitre() != null ? f.getTitre().toLowerCase() : null;
                if (key == null) continue;
                scoresParFiliere.computeIfAbsent(key, k -> new java.util.HashMap<>())
                        .put(r.getTitre(), f.getScoreCompatibilite());
            }
        }

        // 3) Garder uniquement les filières communes (≥ 2 scénarios)
        Map<String, List<ScenarioResult.DeltaParScenario>> deltas = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Double>> e : scoresParFiliere.entrySet()) {
            if (e.getValue().size() < 2) continue;
            List<ScenarioResult.DeltaParScenario> deltasPourFiliere = new java.util.ArrayList<>();
            for (ScenarioResult r : resultats) {
                Double score = e.getValue().get(r.getTitre());
                if (score != null) {
                    deltasPourFiliere.add(new ScenarioResult.DeltaParScenario(r.getTitre(), score));
                }
            }
            deltas.put(e.getKey(), deltasPourFiliere);
        }
        analyse.setDeltasParFiliere(deltas);
        analyse.setNombreFilieresCommunes(deltas.size());

        // 4) Synthèse en langage naturel
        analyse.setSynthese(genererSynthese(analyse, deltas));

        return analyse;
    }

    private String genererSynthese(ScenarioResult.ComparaisonAnalyse analyse,
                                    Map<String, List<ScenarioResult.DeltaParScenario>> deltas) {
        if (analyse.getNombreScenarios() < 2) {
            return "Au moins 2 scénarios sont nécessaires pour une comparaison.";
        }
        if (analyse.getNombreFilieresCommunes() == 0) {
            return "Aucun point commun entre les scénarios : aucune filière n'apparaît dans plus d'un scénario. "
                    + "Essayez de relâcher les contraintes (série, ville, type) pour voir des comparaisons plus parlantes.";
        }
        // Trouver le delta le plus important parmi les filières communes
        String filierePlusVariable = null;
        double deltaMax = 0;
        for (Map.Entry<String, List<ScenarioResult.DeltaParScenario>> e : deltas.entrySet()) {
            List<Double> scores = e.getValue().stream()
                    .map(ScenarioResult.DeltaParScenario::getScore)
                    .collect(Collectors.toList());
            if (scores.size() < 2) continue;
            double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double delta = max - min;
            if (delta > deltaMax) {
                deltaMax = delta;
                filierePlusVariable = e.getKey();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Sur ").append(analyse.getNombreScenarios()).append(" scénarios comparés, ");
        sb.append("le scénario « ").append(analyse.getMeilleurScenario()).append(" » ");
        sb.append("a le meilleur score moyen (").append(analyse.getScoreMoyenMax()).append("/100). ");
        if (filierePlusVariable != null && deltaMax > 0) {
            sb.append("La filière « ").append(filierePlusVariable).append(" » ");
            sb.append("est la plus sensible aux variations : écart de ");
            sb.append(String.format("%.1f", deltaMax)).append(" points entre scénarios.");
        }
        return sb.toString();
    }

    private List<FicheFiliere> trouverFilieres(FicheSerie serie, String motCleMetier) {
        if (serie != null && serie.getFilieresAssociees() != null && !serie.getFilieresAssociees().isEmpty()) {
            List<FicheFiliere> filieres = new ArrayList<>(serie.getFilieresAssociees());
            if (motCleMetier != null && !motCleMetier.isBlank()) {
                return filieres.stream()
                        .filter(f -> f.getMetiersPrepares().stream()
                                .anyMatch(m -> m.getTitre().toLowerCase().contains(motCleMetier.toLowerCase())))
                        .collect(Collectors.toList());
            }
            return filieres;
        }
        List<FicheFiliere> filieres = filiereRepository.findAllByEstPublieTrue(
                org.springframework.data.domain.PageRequest.of(0, 50)).getContent();
        if (serie != null) {
            // Les anciennes données peuvent ne pas encore avoir de liaison
            // serie_filiere. Le champ niveauRequis (ex. « Bac B/G2 ») reste
            // alors une source fiable pour proposer les formations adaptées.
            filieres = filieres.stream()
                    .filter(f -> filiereAccepteSerie(f, serie))
                    .collect(Collectors.toList());
        }
        if (motCleMetier != null && !motCleMetier.isBlank()) {
            String motCleNormalise = normaliserTexte(motCleMetier);
            filieres = filieres.stream()
                    .filter(f -> contientTexte(f.getDebouchesMetiers(), motCleNormalise)
                            || f.getMetiersPrepares().stream()
                            .anyMatch(m -> contientTexte(m.getTitre(), motCleNormalise)))
                    .collect(Collectors.toList());
        }
        return filieres;
    }

    private List<SeuilAdmission> filtrerSeuils(List<SeuilAdmission> tousSeuils, FicheFiliere filiere) {
        return tousSeuils.stream()
                .filter(s -> s.getFiliere() != null
                        && s.getFiliere().getTrackingId().equals(filiere.getTrackingId()))
                .collect(Collectors.toList());
    }

    private int compterSeuilsValides(List<SeuilAdmission> seuils, Map<String, Double> notes) {
        if (seuils.isEmpty() || notes.isEmpty()) return 0;
        int valides = 0;
        for (SeuilAdmission seuil : seuils) {
            String matiere = normaliserMatiere(seuil.getMatiereRequise());
            Double note = trouverNote(matiere, notes);
            if (note != null && note >= seuil.getNoteMinimum()) {
                valides++;
            }
        }
        return valides;
    }

    private boolean verifierNiveauAcces(String niveauEleve, FicheSerie serie, String niveauRequis) {
        if (niveauRequis == null || niveauRequis.isBlank()) return true;
        if (niveauEleve == null || niveauEleve.isBlank()) return true;
        String niveau = normaliserTexte(niveauEleve);
        String requis = normaliserTexte(niveauRequis);
        if (requis.contains(niveau) || niveau.contains(requis)) return true;

        // Une simulation faite en Terminale avec une série sélectionnée vise
        // nécessairement une formation post-bac. « Terminale » et « Bac G2 »
        // ne sont pas des chaînes comparables, mais sont bien compatibles.
        return serie != null && niveau.equals("terminale")
                && (requis.contains("bac") || requis.contains("baccalaureat"))
                && filiereAccepteSerie(niveauRequis, serie);
    }

    private boolean filiereAccepteSerie(FicheFiliere filiere, FicheSerie serie) {
        return filiereAccepteSerie(filiere.getNiveauRequis(), serie);
    }

    private boolean filiereAccepteSerie(String niveauRequis, FicheSerie serie) {
        if (niveauRequis == null || niveauRequis.isBlank() || serie == null) return true;
        String serieNormalisee = normaliserTexte(serie.getTitre()).toUpperCase(Locale.ROOT);
        java.util.regex.Matcher matcher = Pattern.compile("\\b([A-G][0-9]?)\\b").matcher(serieNormalisee);
        if (!matcher.find()) return true;

        String codeSerie = matcher.group(1);
        String requis = normaliserTexte(niveauRequis).toUpperCase(Locale.ROOT);
        if (contientCode(requis, codeSerie)) return true;
        // « Bac G » couvre les spécialités techniques G1, G2 et G3.
        return codeSerie.length() > 1 && contientCode(requis, codeSerie.substring(0, 1));
    }

    private boolean contientCode(String texte, String code) {
        return Pattern.compile("(^|[^A-Z0-9])" + Pattern.quote(code) + "($|[^A-Z0-9])")
                .matcher(texte).find();
    }

    private double calculerScoreCompatibilite(int seuilsValides, int seuilsTotal, int nbNotes, boolean accesNiveau) {
        double score = 50.0;
        if (accesNiveau) score += 15.0;
        if (seuilsTotal > 0) {
            score += 35.0 * ((double) seuilsValides / seuilsTotal);
        } else if (nbNotes == 0) {
            score += 15.0;
        } else {
            score += 20.0;
        }
        return Math.min(score, 100.0);
    }

    private Double trouverNote(String matiereNormalisee, Map<String, Double> notes) {
        if (notes.containsKey(matiereNormalisee)) return notes.get(matiereNormalisee);
        for (Map.Entry<String, Double> entry : notes.entrySet()) {
            if (normaliserMatiere(entry.getKey()).equals(matiereNormalisee)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String normaliserMatiere(String matiere) {
        if (matiere == null) return "";
        return matiere.toLowerCase().trim()
                .replaceAll("\\s+", "")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[ôö]", "o")
                .replaceAll("[îï]", "i")
                .replaceAll("[ç]", "c")
                .replaceAll("-", "")
                .replaceAll("'", "")
                .replaceAll("\"", "");
    }

    private List<ScenarioResult.MetierCible> chargerMetiers(
            Set<String> trackingIds, List<FicheFiliere> filieres, String motCle) {
        List<FicheMetier> metiers = new ArrayList<>();
        for (String id : trackingIds) {
            metierRepository.findByTrackingId(UUID.fromString(id)).ifPresent(metiers::add);
        }
        if (metiers.isEmpty() && !filieres.isEmpty()) {
            // Compatibilité avec les catalogues importés avant la création des
            // relations filiere_metier : les débouchés textuels sont exploités.
            List<String> debouches = filieres.stream()
                    .map(FicheFiliere::getDebouchesMetiers)
                    .filter(Objects::nonNull)
                    .map(this::normaliserTexte)
                    .collect(Collectors.toList());
            metiers = metierRepository.findAll().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getEstPublie()))
                    .filter(m -> debouches.stream()
                            .anyMatch(d -> contientTexte(d, normaliserTexte(m.getTitre()))))
                    .collect(Collectors.toList());
        }
        if (motCle != null && !motCle.isBlank()) {
            metiers = metiers.stream()
                    .filter(m -> m.getTitre().toLowerCase().contains(motCle.toLowerCase())
                            || (m.getSecteur() != null && m.getSecteur().toLowerCase().contains(motCle.toLowerCase())))
                    .collect(Collectors.toList());
        }
        return metiers.stream().map(m -> {
            ScenarioResult.MetierCible mt = new ScenarioResult.MetierCible();
            mt.setTrackingId(m.getTrackingId().toString());
            mt.setTitre(m.getTitre());
            mt.setResume(m.getResume());
            mt.setSecteur(m.getSecteur());
            mt.setFourchetteSalaire(m.getFourchetteSalaire());
            return mt;
        }).collect(Collectors.toList());
    }

    private List<ScenarioResult.EtablissementCible> chargerEtablissements(
            Set<String> trackingIds, List<FicheFiliere> filieres,
            String ville, String typeStr, Boolean estPublic) {
        List<FicheEtablissement> etabs = new ArrayList<>();
        for (String id : trackingIds) {
            etablissementRepository.findByTrackingId(UUID.fromString(id)).ifPresent(etabs::add);
        }
        if (etabs.isEmpty() && !filieres.isEmpty()) {
            etabs = etablissementRepository.findAll().stream()
                    .filter(e -> Boolean.TRUE.equals(e.getEstPublie()))
                    .filter(e -> filieres.stream().anyMatch(f ->
                            contientTexte(e.getOffreFormation(), normaliserTexte(f.getTitre()))))
                    .collect(Collectors.toList());
        }
        if (ville != null && !ville.isBlank()) {
            etabs = etabs.stream()
                    .filter(e -> e.getVille() != null && e.getVille().equalsIgnoreCase(ville))
                    .collect(Collectors.toList());
        }
        if (estPublic != null) {
            etabs = etabs.stream()
                    .filter(e -> Objects.equals(e.getEstPublic(), estPublic))
                    .collect(Collectors.toList());
        }
        if (typeStr != null && !typeStr.isBlank()) {
            etabs = etabs.stream()
                    .filter(e -> e.getTypeEtablissement() != null
                            && e.getTypeEtablissement().name().equalsIgnoreCase(typeStr))
                    .collect(Collectors.toList());
        }
        return etabs.stream().map(e -> {
            ScenarioResult.EtablissementCible et = new ScenarioResult.EtablissementCible();
            et.setTrackingId(e.getTrackingId().toString());
            et.setTitre(e.getTitre());
            et.setVille(e.getVille());
            et.setType(e.getTypeEtablissement() != null ? e.getTypeEtablissement().name() : null);
            et.setNiveau(e.getNiveau());
            et.setEstPublic(e.getEstPublic());
            et.setFilieresProposeesTitres(e.getFilieresProposees().stream()
                    .map(FicheFiliere::getTitre).collect(Collectors.toList()));
            return et;
        }).collect(Collectors.toList());
    }

    private String normaliserTexte(String valeur) {
        if (valeur == null) return "";
        return Normalizer.normalize(valeur, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private boolean contientTexte(String texte, String rechercheNormalisee) {
        return texte != null && !rechercheNormalisee.isBlank()
                && normaliserTexte(texte).contains(rechercheNormalisee);
    }

    private double calculerDureeMin(List<ScenarioResult.FiliereMatch> filieres) {
        return filieres.stream()
                .mapToDouble(f -> extraireDuree(f.getDuree()))
                .min().orElse(0);
    }

    private double calculerDureeMax(List<ScenarioResult.FiliereMatch> filieres) {
        return filieres.stream()
                .mapToDouble(f -> extraireDuree(f.getDuree()))
                .max().orElse(0);
    }

    private double extraireDuree(String duree) {
        if (duree == null) return 3;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(duree);
        return m.find() ? Double.parseDouble(m.group(1)) : 3;
    }
}
