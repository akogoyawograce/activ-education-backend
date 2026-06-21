package tg.edtch.activEducation.bibliotheque.domain.service;

import tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheGlobaleResponse;

import java.util.List;

/**
 * Service de recherche sémantique globale sur l'ensemble des fiches (Métiers,
 * Filières, Établissements, Séries).
 */
public interface RechercheGlobaleService {
    /**
     * Recherche des fiches pertinentes sur la base d'une phrase en langage naturel.
     *
     * @param phrase La question ou phrase de recherche de l'utilisateur.
     * @param limite Le nombre maximum de résultats à retourner.
     * @return Liste ordonnée par pertinence des fiches correspondantes.
     */
    List<RechercheGlobaleResponse> rechercherFichesParPhrase(String phrase, int limite);
}
