package tg.edtch.activEducation.prediction.domain.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Catalogue statique des profils RIASEC typiques pour les principales
 * filières togolaises.
 *
 * <p>Valeurs comprises entre 0 et 1 (0 = dimension absente du profil,
 * 1 = dimension centrale). L'ordre est : {@code R, I, A, S, E, C}
 * (Realiste, Investigateur, Artistique, Social, Entrepreneurial,
 * Conventionnel) — aligné sur {@code TestRIASECResultat}.</p>
 *
 * <p>Couverture : ~46 filières fréquentes. Pour les non répertoriées,
 * retour d'un profil neutre (0.5 sur chaque dimension).</p>
 *
 * <p>Profilé et validé le 18/08/2026 à partir d'O*NET 30.3
 * (moyenne des intérêts RIASEC des métiers appariés, échelle 1-7
 * normalisée en 0-1). Voir donnée/calibration/.</p>
 *
 * <p>Le matching par nom de filière est {@code containsIgnoreCase} :
 * "Informatique de gestion" matchera le profil "Informatique".</p>
 */
public final class ProfilFiliereRiasecCatalog {

    /** Profil neutre : aucune dimension n'émerge, score de similarité moyen. */
    private static final double[] NEUTRE = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};

    private static final Map<String, double[]> PROFILS = new HashMap<>();

    static {
        // ─── Sciences & techniques ──────────────────────────────────────
        PROFILS.put("informatique",      new double[]{0.80, 0.95, 0.30, 0.30, 0.40, 0.60});
        PROFILS.put("mathematiques",     new double[]{0.60, 0.95, 0.30, 0.30, 0.30, 0.70});
        PROFILS.put("physique",          new double[]{0.70, 0.95, 0.30, 0.40, 0.30, 0.65});
        PROFILS.put("genie civil",       new double[]{0.85, 0.42, 0.12, 0.10, 0.22, 0.59});
        PROFILS.put("genie electrique",  new double[]{0.90, 0.38, 0.03, 0.09, 0.18, 0.60});
        PROFILS.put("genie logiciel",    new double[]{0.85, 0.90, 0.35, 0.30, 0.40, 0.55});

        // ─── Santé ──────────────────────────────────────────────────────
        PROFILS.put("medecine",          new double[]{0.55, 0.95, 0.30, 0.90, 0.40, 0.80});
        PROFILS.put("pharmacie",         new double[]{0.55, 0.90, 0.30, 0.75, 0.40, 0.85});
        PROFILS.put("biologie",          new double[]{0.60, 0.90, 0.40, 0.60, 0.30, 0.65});
        PROFILS.put("sante",             new double[]{0.55, 0.85, 0.35, 0.90, 0.40, 0.80});
        PROFILS.put("soins infirmiers",  new double[]{0.60, 0.75, 0.30, 0.95, 0.30, 0.65});

        // ─── Lettres, droit, sciences humaines ───────────────────────────
        PROFILS.put("droit",             new double[]{0.30, 0.70, 0.50, 0.85, 0.80, 0.85});
        PROFILS.put("lettres",           new double[]{0.14, 0.33, 0.40, 0.33, 0.47, 0.70});
        PROFILS.put("communication",     new double[]{0.33, 0.32, 0.44, 0.34, 0.43, 0.55});
        PROFILS.put("psychologie",       new double[]{0.30, 0.75, 0.65, 0.95, 0.50, 0.55});
        PROFILS.put("education",         new double[]{0.20, 0.45, 0.55, 0.95, 0.40, 0.55});
        PROFILS.put("sciences de l'education", new double[]{0.25, 0.50, 0.50, 0.95, 0.35, 0.50});
        PROFILS.put("sociologie",        new double[]{0.30, 0.70, 0.55, 0.85, 0.40, 0.60});
        PROFILS.put("sciences politiques", new double[]{0.12, 0.58, 0.18, 0.33, 0.54, 0.69});
        PROFILS.put("enseignement primaire", new double[]{0.22, 0.35, 0.41, 0.95, 0.34, 0.49});
        PROFILS.put("anglais",           new double[]{0.21, 0.54, 0.45, 0.83, 0.18, 0.51});

        // ─── Gestion, commerce, économie ────────────────────────────────
        PROFILS.put("gestion",           new double[]{0.40, 0.55, 0.40, 0.80, 0.95, 0.85});
        PROFILS.put("economie",          new double[]{0.16, 0.91, 0.24, 0.23, 0.41, 0.62});
        PROFILS.put("commerce",          new double[]{0.45, 0.45, 0.40, 0.85, 0.95, 0.75});
        PROFILS.put("banque",            new double[]{0.09, 0.29, 0.03, 0.37, 0.62, 0.84});
        PROFILS.put("comptabilite",      new double[]{0.25, 0.26, 0.01, 0.22, 0.46, 0.90});
        PROFILS.put("marketing",         new double[]{0.20, 0.40, 0.70, 0.80, 0.90, 0.55});

        // ─── Agriculture, environnement ─────────────────────────────────
        PROFILS.put("agronomie",         new double[]{0.85, 0.65, 0.25, 0.40, 0.35, 0.50});
        PROFILS.put("agriculture",       new double[]{0.85, 0.60, 0.20, 0.35, 0.40, 0.45});
        PROFILS.put("environnement",     new double[]{0.75, 0.70, 0.30, 0.45, 0.30, 0.50});
        PROFILS.put("sciences forestieres", new double[]{0.67, 0.60, 0.14, 0.44, 0.47, 0.52});

        // ─── Arts, design, sport ────────────────────────────────────────
        PROFILS.put("arts",              new double[]{0.55, 0.30, 0.57, 0.36, 0.33, 0.46});
        PROFILS.put("design",            new double[]{0.30, 0.40, 0.90, 0.35, 0.45, 0.30});
        PROFILS.put("architecture",      new double[]{0.70, 0.60, 0.85, 0.30, 0.45, 0.40});
        PROFILS.put("sport",             new double[]{0.75, 0.30, 0.40, 0.70, 0.50, 0.25});

        // ─── Tourisme, hôtellerie ───────────────────────────────────────
        PROFILS.put("tourisme",          new double[]{0.33, 0.07, 0.14, 0.56, 0.60, 0.62});
        PROFILS.put("hotellerie",        new double[]{0.33, 0.07, 0.14, 0.56, 0.60, 0.62});

        // ─── Autres filières fréquentes ─────────────────────────────────
        PROFILS.put("mecanique",         new double[]{0.95, 0.60, 0.15, 0.25, 0.35, 0.55});
        PROFILS.put("electronique",      new double[]{0.85, 0.85, 0.20, 0.30, 0.35, 0.55});
        PROFILS.put("logistique",        new double[]{0.65, 0.40, 0.20, 0.55, 0.70, 0.85});
        PROFILS.put("transport",         new double[]{0.70, 0.35, 0.20, 0.50, 0.65, 0.80});
        PROFILS.put("geographie",        new double[]{0.68, 0.78, 0.28, 0.17, 0.13, 0.66});
        PROFILS.put("histoire",          new double[]{0.20, 0.65, 0.70, 0.55, 0.25, 0.60});
        PROFILS.put("philosophie",       new double[]{0.15, 0.70, 0.75, 0.45, 0.20, 0.50});
        PROFILS.put("journalisme",       new double[]{0.33, 0.32, 0.44, 0.34, 0.43, 0.55});
        PROFILS.put("odontologie",       new double[]{0.73, 0.58, 0.03, 0.53, 0.09, 0.56});
        PROFILS.put("infirmier",         new double[]{0.44, 0.59, 0.10, 0.72, 0.27, 0.52});
    }

    private ProfilFiliereRiasecCatalog() { }

    /**
     * Renvoie le profil RIASEC typique d'une filière, par recherche de
     * sous-chaîne insensible à la casse.
     *
     * @param nomFiliere titre ou libellé de la filière
     * @return vecteur 6-dim dans l'ordre R, I, A, S, E, C. Jamais null.
     */
    public static double[] profilPour(String nomFiliere) {
        if (nomFiliere == null || nomFiliere.isBlank()) {
            return NEUTRE.clone();
        }
        String lower = normaliser(nomFiliere);
        for (Map.Entry<String, double[]> entry : PROFILS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue().clone();
            }
        }
        return NEUTRE.clone();
    }

    /**
     * Normalise un libellé : minuscules + suppression des accents
     * ("Médecine" → "medecine"), pour un matching robuste sur les
     * libellés accentués des filières.
     */
    private static String normaliser(String s) {
        String nfd = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    /** Liste des mots-clés connus (utile pour les tests et la doc). */
    public static java.util.Set<String> motsCles() {
        return PROFILS.keySet();
    }
}
