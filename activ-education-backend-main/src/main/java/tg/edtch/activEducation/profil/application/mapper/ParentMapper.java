package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.ParentRequest;
import tg.edtch.activEducation.profil.application.dto.response.ParentResponse;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.Parent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper dédié à l'entité {@link Parent}.
 * Les enfants sont toujours convertis depuis/vers leurs {@code trackingId} UUID
 * publics.
 */
@Component
public class ParentMapper {

    /**
     * Convertit un {@link ParentRequest} en entité {@link Parent}.
     * Les élèves-enfants ne sont PAS résolus ici — c'est la responsabilité du
     * Service
     * qui effectue les lookups en base via les trackingId fournis.
     */
    public Parent toEntity(ParentRequest request) {
        if (request == null)
            return null;
        return Parent.builder()
                .trackingId(UUID.randomUUID())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .estActif(true)
                .build();
    }

    /**
     * Convertit une entité {@link Parent} en {@link ParentResponse}.
     * Les enfants sont exposés uniquement via leurs trackingId publics — jamais les
     * Long id.
     */
    public ParentResponse toResponse(Parent parent) {
        if (parent == null)
            return null;

        List<UUID> enfantsTrackingIds = (parent.getEnfants() != null)
                ? parent.getEnfants().stream()
                        .map(Eleve::getTrackingId)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return ParentResponse.builder()
                .trackingId(parent.getTrackingId())
                .nom(parent.getNom())
                .prenom(parent.getPrenom())
                .email(parent.getEmail())
                .telephone(parent.getTelephone())
                .enfantsTrackingIds(enfantsTrackingIds)
                .actif(parent.getEstActif())
                .createdAt(parent.getCreatedAt())
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un {@link Parent} existant.
     * L'email et le trackingId sont intentionnellement exclus.
     * La gestion des enfants est déléguée à des méthodes dédiées du Service.
     */
    public void updateFromRequest(ParentRequest request, Parent parent) {
        if (request.getNom() != null)
            parent.setNom(request.getNom());
        if (request.getPrenom() != null)
            parent.setPrenom(request.getPrenom());
        if (request.getTelephone() != null)
            parent.setTelephone(request.getTelephone());
    }
}
