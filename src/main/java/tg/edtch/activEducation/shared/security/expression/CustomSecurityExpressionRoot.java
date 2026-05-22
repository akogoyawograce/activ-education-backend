package tg.edtch.activEducation.shared.security.expression;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.domain.entite.Parent;
import tg.edtch.activEducation.profil.repository.ParentRepository;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

import java.util.List;
import java.util.UUID;

/**
 * Expressions de sécurité personnalisées utilisables dans @PreAuthorize.
 * Exemples d'utilisation :
 * @PreAuthorize("@security.isOwner(#trackingId)")
 * @PreAuthorize("@security.isOwnChild(#eleveTrackingId)")
 * @PreAuthorize("hasRole('ADMIN') or @security.isOwner(#trackingId)")
 */
@Component("security")
@RequiredArgsConstructor
public class CustomSecurityExpressionRoot {

    private final ParentRepository parentRepository;

    /**
     * Vérifie si l'utilisateur connecté est le propriétaire de la ressource.
     */
    public boolean isOwner(UUID trackingId) {
        CustomUserDetails userDetails = getCurrentUser();
        if (userDetails == null)
            return false;
        return userDetails.getTrackingId().equals(trackingId);
    }

    /**
     * Vérifie si l'élève identifié par eleveTrackingId est un enfant
     * du parent actuellement connecté.
     * Retourne false si l'utilisateur n'est pas un Parent.
     */
    public boolean isOwnChild(UUID eleveTrackingId) {
        CustomUserDetails userDetails = getCurrentUser();
        if (userDetails == null)
            return false;

        if (!"Parent".equals(userDetails.getTypeUtilisateur())) {
            return false;
        }

        // Vérifier que le parent connecté est bien dans la liste des parents de cet
        // élève
        List<Parent> parents = parentRepository.findParentsByEleveTrackingId(eleveTrackingId);
        return parents.stream()
                .anyMatch(p -> p.getId().equals(userDetails.getId()));
    }

    /**
     * Vérifie si le conseiller identifié par conseillerTrackingId
     * est bien le conseiller associé à l'utilisateur connecté (via un rendez-vous).
     * Note : cette vérification devrait idéalement passer par le
     * RendezVousRepository,
     * mais pour éviter un couplage fort avec le module accompagnement, la
     * vérification
     * se fait au niveau du service métier via @PreAuthorize côté controller.
     * Ici on vérifie simplement que l'utilisateur est bien le conseiller lui-même.
     */
    public boolean isOwnConseiller(UUID conseillerTrackingId) {
        CustomUserDetails userDetails = getCurrentUser();
        if (userDetails == null)
            return false;

        // Un conseiller accédant à ses propres données
        if ("Conseiller".equals(userDetails.getTypeUtilisateur())) {
            return userDetails.getTrackingId().equals(conseillerTrackingId);
        }

        return false;
    }

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
