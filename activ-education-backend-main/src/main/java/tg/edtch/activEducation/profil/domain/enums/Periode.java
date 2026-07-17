package tg.edtch.activEducation.profil.domain.enums;

/**
 * Période de l'année scolaire au moment de l'upload d'un bulletin.
 *
 * <p>Utilisé par le front pour indiquer "où on en est" dans l'année :
 * l'élève a-t-il déjà passé des examens ou non ? Le backend s'en sert
 * pour dériver le {@code semestreOuTrimestre} (Trimestre 1 / 2 / 3 ou
 * Semestre 1 / 2) à enregistrer sur les {@code NoteSaisiManuel} issues
 * de l'OCR.</p>
 *
 * <p>Mapping (cf. {@code BulletinUploadOrchestrator#buildSemestreLabel}) :
 * <ul>
 *   <li>{@code DEBUT} — début d'année, pas encore d'examens : on enregistre
 *       sur "Trimestre 1" / "Semestre 1" (données partielles, attendues).</li>
 *   <li>{@code MILIEU} — milieu d'année : T1/S1 passés, le bulletin porte
 *       les notes du 1er trimestre/semestre.</li>
 *   <li>{@code FIN} — fin d'année : T2 ou T3/S2 passés selon le système.</li>
 * </ul>
 */
public enum Periode {
    DEBUT("Début d'année"),
    MILIEU("Milieu d'année"),
    FIN("Fin d'année");

    private final String label;

    Periode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
