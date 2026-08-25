package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;

import java.util.UUID;

/**
 * Listes "Explorer" personnalisées selon le profil de l'élève connecté.
 *
 * <p>Quand un {@code eleveTrackingId} valide est fourni, les fiches
 * (filières et séries) sont triées par pertinence vis-à-vis du profil :
 * filière actuelle, métier souhaité, matières préférées, et profil RIASEC
 * si disponible (cosinus avec le catalogue
 * {@code ProfilFiliereRiasecCatalog}).</p>
 *
 * <p>Repli générique : élève introuvable ou profil non exploitable
 * (aucun niveau/filière/métier/matières renseigné) → tri historique
 * (createdAt DESC), liste toujours non vide et sans erreur.</p>
 */
public interface FicheExplorerPersonnaliseeService {

    Page<FicheFiliereResponse> listerFilieresPersonnalisees(UUID eleveTrackingId, Pageable pageable);

    Page<FicheSerieResponse> listerSeriesPersonnalisees(UUID eleveTrackingId, Pageable pageable);
}
