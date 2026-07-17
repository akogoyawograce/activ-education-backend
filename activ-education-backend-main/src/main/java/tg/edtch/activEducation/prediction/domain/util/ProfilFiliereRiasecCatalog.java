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
 * <p><strong>Limites :</strong> mapping en dur pour 15 filières. Pour
 * les autres, on retourne un profil neutre (0.5 sur chaque dimension).
 * À raffiner avec les retours conseillers et les données réelles
 * (Phase 5).</p>
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
        // Très Réaliste + Investigateur, peu social/artistique
        PROFILS.put("informatique", new double[]{0.80, 0.95, 0.30, 0.30, 0.40, 0.60});
        PROFILS.put("mathematiques", new double[]{0.60, 0.95, 0.30, 0.30, 0.30, 0.70});
        PROFILS.put("physique",      new double[]{0.70, 0.95, 0.30, 0.40, 0.30, 0.65});
        PROFILS.put("genie civil",   new double[]{0.90, 0.75, 0.20, 0.40, 0.50, 0.60});
        PROFILS.put("genie electrique", new double[]{0.85, 0.85, 0.25, 0.35, 0.40, 0.60});

        // ─── Santé ──────────────────────────────────────────────────────
        // Très Investigateur + Social, dimension Conventionnel forte
        // (rigueur, protocoles)
        PROFILS.put("medecine",      new double[]{0.55, 0.95, 0.30, 0.90, 0.40, 0.80});
        PROFILS.put("pharmacie",     new double[]{0.55, 0.90, 0.30, 0.75, 0.40, 0.85});
        PROFILS.put("biologie",      new double[]{0.60, 0.90, 0.40, 0.60, 0.30, 0.65});
        PROFILS.put("sante",         new double[]{0.55, 0.85, 0.35, 0.90, 0.40, 0.80});

        // ─── Lettres, droit, sciences humaines ───────────────────────────
        // Artistique + Social + Investigateur (texte)
        PROFILS.put("droit",         new double[]{0.30, 0.70, 0.50, 0.85, 0.80, 0.85});
        PROFILS.put("lettres",       new double[]{0.20, 0.60, 0.90, 0.75, 0.40, 0.60});
        PROFILS.put("communication", new double[]{0.20, 0.50, 0.90, 0.95, 0.70, 0.50});
        PROFILS.put("psychologie",   new double[]{0.30, 0.75, 0.65, 0.95, 0.50, 0.55});

        // ─── Gestion, commerce, économie ────────────────────────────────
        // Entrepreneurial + Social + Conventionnel
        PROFILS.put("gestion",       new double[]{0.40, 0.55, 0.40, 0.80, 0.95, 0.85});
        PROFILS.put("economie",      new double[]{0.35, 0.80, 0.35, 0.65, 0.80, 0.85});
        PROFILS.put("commerce",      new double[]{0.45, 0.45, 0.40, 0.85, 0.95, 0.75});
        PROFILS.put("comptabilite",  new double[]{0.40, 0.55, 0.25, 0.55, 0.65, 0.95});
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
