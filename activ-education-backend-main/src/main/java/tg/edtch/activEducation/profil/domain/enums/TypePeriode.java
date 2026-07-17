package tg.edtch.activEducation.profil.domain.enums;

/**
 * Type de découpage de l'année scolaire (le Togo utilise les 2).
 *
 * <p>{@code TRIMESTRE} = 3 périodes (T1, T2, T3) — majorité des lycées.
 * {@code SEMESTRE} = 2 périodes (S1, S2) — université, certaines écoles.</p>
 *
 * <p>Le backend dérive le {@code semestreOuTrimestre} final
 * (ex. "Trimestre 2") à partir de la combinaison
 * {@code TypePeriode} + {@code Periode} + {@code numeroPeriode}.</p>
 */
public enum TypePeriode {
    TRIMESTRE,
    SEMESTRE
}
