package tg.edtch.activEducation.simulateur.application.service;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.simulateur.application.dto.ScenarioTemplate;
import tg.edtch.activEducation.simulateur.application.dto.ScenarioTemplate.CategorieTemplate;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Registre statique des scénarios types proposés aux élèves.
 *
 * <p>Pas de persistance : les 6 templates sont hardcodés en code
 * (cf. cahier des charges — c'est un point d'entrée non-personnalisé
 * qui n'a pas vocation à être modifié par les admins à court terme).</p>
 *
 * <p>L'ordering est important : la liste est ordonnée par catégorie
 * (cf. {@link CategorieTemplate#ordinal()}) puis par titre. Le front
 * peut donc itérer sans trier côté serveur.</p>
 */
@Component
public class ScenarioTemplateRegistry {

    /**
     * Map ordonnée trackingId → template. LinkedHashMap pour préserver
     * l'ordre d'insertion (utilisé par {@link #lister()}).
     */
    private final Map<UUID, ScenarioTemplate> templates = new LinkedHashMap<>();

    public ScenarioTemplateRegistry() {
        // 6 templates hardcodés.
        // Note : on génère les trackingId à la construction pour qu'ils
        // soient stables entre les redémarrages (UUID v4, mais une seule
        // génération au démarrage du bean).
        templates.put(UUID.fromString("11111111-1111-1111-1111-000000000001"),
                buildProgressionMaths());
        templates.put(UUID.fromString("11111111-1111-1111-1111-000000000002"),
                buildMobiliteLome());
        templates.put(UUID.fromString("11111111-1111-1111-1111-000000000003"),
                buildFiliereCourte());
        templates.put(UUID.fromString("11111111-1111-1111-1111-000000000004"),
                buildTransitionSerie());
        templates.put(UUID.fromString("11111111-1111-1111-1111-000000000005"),
                buildAjoutAnglais());
        templates.put(UUID.fromString("11111111-1111-1111-1111-000000000006"),
                buildEcolePrivee());
    }

    public List<ScenarioTemplate> lister() {
        return templates.values().stream()
                .sorted((a, b) -> {
                    int cat = a.categorie().ordinal() - b.categorie().ordinal();
                    return cat != 0 ? cat : a.titre().compareTo(b.titre());
                })
                .collect(Collectors.toList());
    }

    public ScenarioTemplate get(UUID trackingId) {
        ScenarioTemplate t = templates.get(trackingId);
        if (t == null) {
            throw new NoSuchElementException("Template introuvable : " + trackingId);
        }
        return t;
    }

    // ─── Builders privés (un par template) ───────────────────────────────

    private static ScenarioTemplate buildProgressionMaths() {
        return new ScenarioTemplate(
                UUID.fromString("11111111-1111-1111-1111-000000000001"),
                "Et si je montais ma moyenne de maths de 2 points ?",
                "Mes notes actuelles en maths me ferment certaines filières. "
                        + "Ce scénario simule une progression de +2 points sur les notes "
                        + "de maths pour voir quelles portes s'ouvrent.",
                CategorieTemplate.PROGRESSION_NOTES,
                () -> {
                    ScenarioRequest r = new ScenarioRequest();
                    r.setTitre("Maths +2 points");
                    r.setMotCleMetier(null);
                    r.setNotesSimulees(Map.of("Mathematiques", 14.0));
                    return r;
                });
    }

    private static ScenarioTemplate buildMobiliteLome() {
        return new ScenarioTemplate(
                UUID.fromString("11111111-1111-1111-1111-000000000002"),
                "Et si j'allais à Lomé au lieu de Kara ?",
                "Je simule un déménagement à Lomé : beaucoup plus d'établissements "
                        + "disponibles, mais plus de concurrence. Quel impact sur mes options ?",
                CategorieTemplate.MOBILITE_GEO,
                () -> {
                    ScenarioRequest r = new ScenarioRequest();
                    r.setTitre("Mobilité Lomé");
                    r.setVille("Lomé");
                    return r;
                });
    }

    private static ScenarioTemplate buildFiliereCourte() {
        return new ScenarioTemplate(
                UUID.fromString("11111111-1111-1111-1111-000000000003"),
                "Et si je choisissais une filière courte (BTS/DUT) ?",
                "Au lieu d'une licence 3 ans, je vise un BTS ou DUT (2 ans). "
                        + "Plus rapide, plus professionnalisant, mais moins de poursuites "
                        + "d'études possibles.",
                CategorieTemplate.ALTERNATIVE_FILIERE,
                () -> {
                    ScenarioRequest r = new ScenarioRequest();
                    r.setTitre("Filière courte BTS/DUT");
                    r.setMotCleMetier("BTS");
                    return r;
                });
    }

    private static ScenarioTemplate buildTransitionSerie() {
        return new ScenarioTemplate(
                UUID.fromString("11111111-1111-1111-1111-000000000004"),
                "Et si je passais de la série C à la série D ?",
                "Je simule un changement de série : la D ouvre plus de passerelles "
                        + "vers les sciences biologiques / agronomie que la C.",
                CategorieTemplate.TRANSITION_SERIE,
                () -> {
                    ScenarioRequest r = new ScenarioRequest();
                    r.setTitre("Passage série C → D");
                    r.setNiveau("LYCEE_1ERE");
                    return r;
                });
    }

    private static ScenarioTemplate buildAjoutAnglais() {
        return new ScenarioTemplate(
                UUID.fromString("11111111-1111-1111-1111-000000000005"),
                "Et si j'ajoutais l'anglais LV2 ?",
                "J'ajoute l'anglais comme matière pour élargir les établissements "
                        + "potentiels (beaucoup exigent anglais LV1 ou LV2).",
                CategorieTemplate.OPTION_MATIERE,
                () -> {
                    ScenarioRequest r = new ScenarioRequest();
                    r.setTitre("Avec anglais LV2");
                    r.setNotesSimulees(Map.of("Anglais", 13.0, "Mathematiques", 12.0));
                    return r;
                });
    }

    private static ScenarioTemplate buildEcolePrivee() {
        return new ScenarioTemplate(
                UUID.fromString("11111111-1111-1111-1111-000000000006"),
                "Et si je visais une école privée ?",
                "Je restreins aux établissements privés. Plus de frais, mais "
                        + "souvent des formations plus spécialisées et un meilleur "
                        + "accompagnement.",
                CategorieTemplate.ETABLISSEMENT,
                () -> {
                    ScenarioRequest r = new ScenarioRequest();
                    r.setTitre("Cible écoles privées");
                    r.setEtablissementPublic(false);
                    return r;
                });
    }
}
