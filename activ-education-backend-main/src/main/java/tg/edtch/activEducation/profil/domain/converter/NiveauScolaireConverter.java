package tg.edtch.activEducation.profil.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;

/**
 * Converter JPA pour le champ {@code niveau} de l'entité {@code Eleve}.
 *
 * <p>Justification : on garde une colonne VARCHAR en base (rétrocompatibilité
 * avec les données déjà saisies par les utilisateurs) tout en typant
 * strictement côté Java via l'enum {@link NiveauScolaire}. Le parsing
 * tolérant {@link NiveauScolaire#parse(String)} permet d'absorber les
 * anciens libellés libres ("Terminale C", "Licence 2 Informatique", ...).</p>
 *
 * <p>{@code autoApply=true} propage le converter à tous les champs
 * {@code NiveauScolaire} du modèle.</p>
 *
 * <p>Voir {@code CHANGELOG_SCHEMA.md} § 1 et § 7.</p>
 */
@Converter(autoApply = true)
public class NiveauScolaireConverter implements AttributeConverter<NiveauScolaire, String> {

    @Override
    public String convertToDatabaseColumn(NiveauScolaire niveau) {
        return niveau == null ? null : niveau.name();
    }

    @Override
    public NiveauScolaire convertToEntityAttribute(String dbValue) {
        return NiveauScolaire.parse(dbValue);
    }
}
