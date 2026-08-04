package tg.edtch.activEducation.shared.util;

/**
 * Utilitaire de sérialisation d'un vecteur d'embedding au format littéral pgvector.
 *
 * <p>Une requête JPA native avec {@code CAST(:vecteur AS vector)} attend une chaîne
 * de la forme {@code [v0,v1,v2,...]} (format <em>array literal</em> PostgreSQL).
 * On ne peut pas binder directement un {@code float[]} en paramètre Spring Data.</p>
 *
 * <p>Le helper gère un vecteur potentiellement nul (renvoie {@code null} pour permettre
 * au repo d'ignorer la recherche vectorielle via un guard).</p>
 */
public final class PgVectorLiteral {

    private PgVectorLiteral() {
        // utilitaire statique
    }

    /**
     * Sérialise un vecteur au format pgvector literal.
     *
     * @param vecteur vecteur d'embedding (peut être null)
     * @return chaîne {@code [v0,v1,...]} ou {@code null} si vecteur null/vide
     */
    public static String toVectorLiteral(float[] vecteur) {
        if (vecteur == null || vecteur.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(vecteur.length * 8);
        sb.append('[');
        for (int i = 0; i < vecteur.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            // Float.toString garantit la précision ; pas de Locale pour stabilité
            sb.append(Float.toString(vecteur[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}