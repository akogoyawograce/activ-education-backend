package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.ConseillerRequest;
import tg.edtch.activEducation.profil.application.dto.response.ConseillerResponse;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Conseiller}.
 * Toutes les conversions Request ↔ Entité et Entité ↔ Response sont
 * centralisées ici.
 */
@Component
public class ConseillerMapper {

    /**
     * Convertit un {@link ConseillerRequest} en entité {@link Conseiller}.
     * Un {@code trackingId} unique est généré automatiquement.
     * Le mot de passe n'est pas encodé ici — c'est la responsabilité du Service.
     */
    public Conseiller toEntity(ConseillerRequest request) {
        if (request == null)
            return null;
        return Conseiller.builder()
                .trackingId(UUID.randomUUID())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .specialites(request.getSpecialites())
                .biographie(request.getBiographie())
                .qualifications(request.getQualifications())
                .anneesExperience(request.getAnneesExperience())
                .chargeTravail(0)
                .estActif(true)
                .build();
    }

    /**
     * Convertit une entité {@link Conseiller} en {@link ConseillerResponse}.
     * La clé primaire Long n'est jamais incluse dans la réponse.
     */
    public ConseillerResponse toResponse(Conseiller conseiller) {
        if (conseiller == null)
            return null;
        return ConseillerResponse.builder()
                .trackingId(conseiller.getTrackingId())
                .nom(conseiller.getNom())
                .prenom(conseiller.getPrenom())
                .email(conseiller.getEmail())
                .telephone(conseiller.getTelephone())
                .specialites(conseiller.getSpecialites())
                .biographie(conseiller.getBiographie())
                .qualifications(conseiller.getQualifications())
                .anneesExperience(conseiller.getAnneesExperience())
                .chargeTravail(conseiller.getChargeTravail())
                .actif(conseiller.getEstActif())
                .createdAt(conseiller.getCreatedAt())
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un {@link Conseiller} existant.
     * L'email et le trackingId sont intentionnellement exclus (non modifiables).
     */
    public void updateFromRequest(ConseillerRequest request, Conseiller conseiller) {
        if (request.getNom() != null)
            conseiller.setNom(request.getNom());
        if (request.getPrenom() != null)
            conseiller.setPrenom(request.getPrenom());
        if (request.getTelephone() != null)
            conseiller.setTelephone(request.getTelephone());
        if (request.getSpecialites() != null)
            conseiller.setSpecialites(request.getSpecialites());
        if (request.getBiographie() != null)
            conseiller.setBiographie(request.getBiographie());
        if (request.getQualifications() != null)
            conseiller.setQualifications(request.getQualifications());
        if (request.getAnneesExperience() != null)
            conseiller.setAnneesExperience(request.getAnneesExperience());
    }
}
