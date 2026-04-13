package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.EleveRequest;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;
import tg.edtch.activEducation.profil.domain.entite.Eleve;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Eleve}.
 * Toutes les conversions entre Request ↔ Entité et Entité ↔ Response sont
 * centralisées ici.
 */
@Component
public class EleveMapper {

    /**
     * Convertit un {@link EleveRequest} en entité {@link Eleve}.
     * Un nouveau {@code trackingId} est généré automatiquement.
     * Le mot de passe brut est passé tel quel — l'encodage est délégué au Service.
     */
    public Eleve toEntity(EleveRequest request) {
        if (request == null)
            return null;
        return Eleve.builder()
                .trackingId(UUID.randomUUID())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .niveau(request.getNiveauEtude())
                .etablissement(request.getEtablissementActuel())
                .filiere(request.getFiliere())
                .estActif(true)
                .build();
    }

    /**
     * Convertit une entité {@link Eleve} en {@link EleveResponse}.
     * Le Long id interne n'est jamais inclus dans la réponse.
     */
    public EleveResponse toResponse(Eleve eleve) {
        if (eleve == null)
            return null;
        return EleveResponse.builder()
                .trackingId(eleve.getTrackingId())
                .nom(eleve.getNom())
                .prenom(eleve.getPrenom())
                .email(eleve.getEmail())
                .telephone(eleve.getTelephone())
                .niveauEtude(eleve.getNiveau())
                .etablissementActuel(eleve.getEtablissement())
                .filiere(eleve.getFiliere())
                .anneeObtentionPrevue(eleve.getAnneeObtentionPrevue())
                .actif(eleve.getEstActif())
                .createdAt(eleve.getCreatedAt())
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un {@link Eleve} existant depuis un
     * {@link EleveRequest}.
     * L'email et le trackingId sont intentionnellement exclus (non modifiables).
     */
    public void updateFromRequest(EleveRequest request, Eleve eleve) {
        if (request.getNom() != null)
            eleve.setNom(request.getNom());
        if (request.getPrenom() != null)
            eleve.setPrenom(request.getPrenom());
        if (request.getTelephone() != null)
            eleve.setTelephone(request.getTelephone());
        if (request.getNiveauEtude() != null)
            eleve.setNiveau(request.getNiveauEtude());
        if (request.getEtablissementActuel() != null)
            eleve.setEtablissement(request.getEtablissementActuel());
        if (request.getFiliere() != null)
            eleve.setFiliere(request.getFiliere());
    }
}
