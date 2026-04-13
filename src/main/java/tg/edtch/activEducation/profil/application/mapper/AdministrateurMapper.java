package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.AdministrateurRequest;
import tg.edtch.activEducation.profil.application.dto.response.AdministrateurResponse;
import tg.edtch.activEducation.profil.domain.entite.Administrateur;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Administrateur}.
 */
@Component
public class AdministrateurMapper {

    /**
     * Convertit un {@link AdministrateurRequest} en entité {@link Administrateur}.
     * Un {@code trackingId} unique est généré. Le mot de passe sera encodé par le
     * Service.
     */
    public Administrateur toEntity(AdministrateurRequest request) {
        if (request == null)
            return null;
        String niveau = (request.getNiveauAcces() != null) ? request.getNiveauAcces() : "MODERATEUR";
        return Administrateur.builder()
                .trackingId(UUID.randomUUID())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .niveauAcces(niveau)
                .estActif(true)
                .build();
    }

    /**
     * Convertit une entité {@link Administrateur} en
     * {@link AdministrateurResponse}.
     * La clé primaire Long n'est jamais incluse dans la réponse.
     */
    public AdministrateurResponse toResponse(Administrateur admin) {
        if (admin == null)
            return null;
        return AdministrateurResponse.builder()
                .trackingId(admin.getTrackingId())
                .nom(admin.getNom())
                .prenom(admin.getPrenom())
                .email(admin.getEmail())
                .telephone(admin.getTelephone())
                .niveauAcces(admin.getNiveauAcces())
                .actif(admin.getEstActif())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un {@link Administrateur} existant.
     * L'email et le trackingId ne sont jamais modifiés.
     */
    public void updateFromRequest(AdministrateurRequest request, Administrateur admin) {
        if (request.getNom() != null)
            admin.setNom(request.getNom());
        if (request.getPrenom() != null)
            admin.setPrenom(request.getPrenom());
        if (request.getTelephone() != null)
            admin.setTelephone(request.getTelephone());
        if (request.getNiveauAcces() != null)
            admin.setNiveauAcces(request.getNiveauAcces());
    }
}
