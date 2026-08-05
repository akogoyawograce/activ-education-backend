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
 * <p>Couverture : ~40 filières fréquentes. Pour les non répertoriées,
 * retour d'un profil neutre (0.5 sur chaque dimension).</p>
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
        PROFILS.put("genie civil",       new double[]{0.90, 0.75, 0.20, 0.40, 0.50, 0.60});
        PROFILS.put("genie electrique",  new double[]{0.85, 0.85, 0.25, 0.35, 0.40, 0.60});
        PROFILS.put("genie logiciel",    new double[]{0.85, 0.90, 0.35, 0.30, 0.40, 0.55});

        // ─── Santé ──────────────────────────────────────────────────────
        PROFILS.put("medecine",          new double[]{0.55, 0.95, 0.30, 0.90, 0.40, 0.80});
        PROFILS.put("pharmacie",         new double[]{0.55, 0.90, 0.30, 0.75, 0.40, 0.85});
        PROFILS.put("biologie",          new double[]{0.60, 0.90, 0.40, 0.60, 0.30, 0.65});
        PROFILS.put("sante",             new double[]{0.55, 0.85, 0.35, 0.90, 0.40, 0.80});
        PROFILS.put("soins infirmiers",  new double[]{0.60, 0.75, 0.30, 0.95, 0.30, 0.65});

        // ─── Lettres, droit, sciences humaines ───────────────────────────
        PROFILS.put("droit",             new double[]{0.30, 0.70, 0.50, 0.85, 0.80, 0.85});
        PROFILS.put("lettres",           new double[]{0.20, 0.60, 0.90, 0.75, 0.40, 0.60});
        PROFILS.put("communication",     new double[]{0.20, 0.50, 0.90, 0.95, 0.70, 0.50});
        PROFILS.put("psychologie",       new double[]{0.30, 0.75, 0.65, 0.95, 0.50, 0.55});
        PROFILS.put("education",         new double[]{0.20, 0.45, 0.55, 0.95, 0.40, 0.55});
        PROFILS.put("sciences de l'education", new double[]{0.25, 0.50, 0.50, 0.95, 0.35, 0.50});
        PROFILS.put("sociologie",        new double[]{0.30, 0.70, 0.55, 0.85, 0.40, 0.60});

        // ─── Gestion, commerce, économie ────────────────────────────────
        PROFILS.put("gestion",           new double[]{0.40, 0.55, 0.40, 0.80, 0.95, 0.85});
        PROFILS.put("economie",          new double[]{0.35, 0.80, 0.35, 0.65, 0.80, 0.85});
        PROFILS.put("commerce",          new double[]{0.45, 0.45, 0.40, 0.85, 0.95, 0.75});
        PROFILS.put("comptabilite",      new double[]{0.40, 0.55, 0.25, 0.55, 0.65, 0.95});
        PROFILS.put("marketing",         new double[]{0.20, 0.40, 0.70, 0.80, 0.90, 0.55});

        // ─── Agriculture, environnement ─────────────────────────────────
        PROFILS.put("agronomie",         new double[]{0.85, 0.65, 0.25, 0.40, 0.35, 0.50});
        PROFILS.put("agriculture",       new double[]{0.85, 0.60, 0.20, 0.35, 0.40, 0.45});
        PROFILS.put("environnement",     new double[]{0.75, 0.70, 0.30, 0.45, 0.30, 0.50});

        // ─── Arts, design, sport ────────────────────────────────────────
        PROFILS.put("arts",              new double[]{0.25, 0.30, 0.95, 0.40, 0.35, 0.20});
        PROFILS.put("design",            new double[]{0.30, 0.40, 0.90, 0.35, 0.45, 0.30});
        PROFILS.put("architecture",      new double[]{0.70, 0.60, 0.85, 0.30, 0.45, 0.40});
        PROFILS.put("sport",             new double[]{0.75, 0.30, 0.40, 0.70, 0.50, 0.25});

        // ─── Tourisme, hôtellerie ───────────────────────────────────────
        PROFILS.put("tourisme",          new double[]{0.25, 0.35, 0.45, 0.90, 0.75, 0.50});
        PROFILS.put("hotellerie",        new double[]{0.35, 0.30, 0.40, 0.85, 0.80, 0.60});

        // ─── Autres filières fréquentes ─────────────────────────────────
        PROFILS.put("mecanique",         new double[]{0.95, 0.60, 0.15, 0.25, 0.35, 0.55});
        PROFILS.put("electronique",      new double[]{0.85, 0.85, 0.20, 0.30, 0.35, 0.55});
        PROFILS.put("logistique",        new double[]{0.65, 0.40, 0.20, 0.55, 0.70, 0.85});
        PROFILS.put("transport",         new double[]{0.70, 0.35, 0.20, 0.50, 0.65, 0.80});
        PROFILS.put("geographie",        new double[]{0.50, 0.75, 0.55, 0.60, 0.30, 0.50});
        PROFILS.put("histoire",          new double[]{0.20, 0.65, 0.70, 0.55, 0.25, 0.60});
        PROFILS.put("philosophie",       new double[]{0.15, 0.70, 0.75, 0.45, 0.20, 0.50});
        PROFILS.put("journalisme",       new double[]{0.20, 0.55, 0.85, 0.80, 0.60, 0.50});
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
        String lower = nomFiliere.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, double[]> entry : PROFILS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue().clone();
            }
        }
        return NEUTRE.clone();
    }

    /** Liste des mots-clés connus (utile pour les tests et la doc). */
    public static java.util.Set<String> motsCles() {
        return PROFILS.keySet();
    }
}
