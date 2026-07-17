package tg.edtch.activEducation.reseau.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.reseau.domain.dto.*;
import tg.edtch.activEducation.reseau.domain.entite.AbonnementReseau;
import tg.edtch.activEducation.reseau.domain.entite.CommentaireReseau;
import tg.edtch.activEducation.reseau.domain.entite.PublicationReseau;
import tg.edtch.activEducation.reseau.repository.AbonnementReseauRepository;
import tg.edtch.activEducation.reseau.repository.CommentaireReseauRepository;
import tg.edtch.activEducation.reseau.repository.PublicationReseauRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class ReseauService {

    private final PublicationReseauRepository publicationRepository;
    private final CommentaireReseauRepository commentaireRepository;
    private final AbonnementReseauRepository abonnementRepository;

    public ReseauService(PublicationReseauRepository publicationRepository,
                         CommentaireReseauRepository commentaireRepository,
                         AbonnementReseauRepository abonnementRepository) {
        this.publicationRepository = publicationRepository;
        this.commentaireRepository = commentaireRepository;
        this.abonnementRepository = abonnementRepository;
    }

    // ─── Publications ────────────────────────────────────────────────────

    public Page<PublicationResponse> getFeed(String utilisateurId, int page, int size) {
        var abonnements = abonnementRepository.findByAbonneTrackingId(utilisateurId)
            .stream().map(AbonnementReseau::getAbonnementTrackingId).toList();
        if (abonnements.isEmpty()) {
            return publicationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(p -> toPublicationResponse(p, utilisateurId));
        }
        return publicationRepository.findFeedAbonnements(abonnements, PageRequest.of(page, size))
            .map(p -> toPublicationResponse(p, utilisateurId));
    }

    public Page<PublicationResponse> getTendances(int page, int size) {
        return publicationRepository.findTendances(PageRequest.of(page, size))
            .map(p -> toPublicationResponse(p, null));
    }

    public Page<PublicationResponse> getPublicationsUtilisateur(String auteurId, int page, int size, String currentUserId) {
        return publicationRepository.findByAuteurTrackingIdOrderByCreatedAtDesc(auteurId, PageRequest.of(page, size))
            .map(p -> toPublicationResponse(p, currentUserId));
    }

    public PublicationResponse publier(String auteurId, String auteurNom, String auteurRole, PublicationRequest req) {
        var entity = PublicationReseau.builder()
            .auteurTrackingId(auteurId)
            .auteurNom(auteurNom)
            .auteurRole(auteurRole)
            .contenu(req.contenu())
            .typePublication(req.typePublication() != null ? req.typePublication() : "PUBLICATION")
            .tags(req.tags())
            .build();
        return toPublicationResponse(publicationRepository.save(entity), auteurId);
    }

    public void supprimerPublication(UUID trackingId, String utilisateurId) {
        var pub = publicationRepository.findByTrackingId(trackingId)
            .orElseThrow(() -> new NoSuchElementException("Publication introuvable"));
        if (!pub.getAuteurTrackingId().equals(utilisateurId)) {
            throw new SecurityException("Vous n'êtes pas l'auteur de cette publication");
        }
        publicationRepository.deleteByTrackingId(trackingId);
    }

    public void reactionner(UUID publicationTrackingId, String utilisateurId) {
        var pub = publicationRepository.findByTrackingId(publicationTrackingId)
            .orElseThrow(() -> new NoSuchElementException("Publication introuvable"));
        pub.setNombreReactions(pub.getNombreReactions() + 1);
        publicationRepository.save(pub);
    }

    // ─── Commentaires ────────────────────────────────────────────────────

    public Page<CommentaireResponse> getCommentaires(UUID publicationTrackingId, int page, int size) {
        return commentaireRepository.findByPublicationTrackingIdOrderByCreatedAtDesc(
                publicationTrackingId.toString(), PageRequest.of(page, size))
            .map(this::toCommentaireResponse);
    }

    public CommentaireResponse commenter(UUID publicationTrackingId, String auteurId, String auteurNom, CommentaireRequest req) {
        var pub = publicationRepository.findByTrackingId(publicationTrackingId)
            .orElseThrow(() -> new NoSuchElementException("Publication introuvable"));

        var entity = CommentaireReseau.builder()
            .publicationTrackingId(publicationTrackingId.toString())
            .auteurTrackingId(auteurId)
            .auteurNom(auteurNom)
            .contenu(req.contenu())
            .build();
        var saved = commentaireRepository.save(entity);

        pub.setNombreCommentaires(pub.getNombreCommentaires() + 1);
        publicationRepository.save(pub);

        return toCommentaireResponse(saved);
    }

    public void supprimerCommentaire(UUID trackingId, String utilisateurId) {
        var comment = commentaireRepository.findByTrackingId(trackingId);
        comment.ifPresent(c -> {
            if (!c.getAuteurTrackingId().equals(utilisateurId)) {
                throw new SecurityException("Vous n'êtes pas l'auteur de ce commentaire");
            }
            commentaireRepository.deleteByTrackingId(trackingId);
        });
    }

    // ─── Abonnements ─────────────────────────────────────────────────────

    public void suivre(String abonneId, String abonnementId) {
        if (abonneId.equals(abonnementId)) return;
        if (!abonnementRepository.existsByAbonneTrackingIdAndAbonnementTrackingId(abonneId, abonnementId)) {
            abonnementRepository.save(AbonnementReseau.builder()
                .abonneTrackingId(abonneId)
                .abonnementTrackingId(abonnementId)
                .build());
        }
    }

    public void nePlusSuivre(String abonneId, String abonnementId) {
        abonnementRepository.findByAbonneTrackingIdAndAbonnementTrackingId(abonneId, abonnementId)
            .ifPresent(abonnementRepository::delete);
    }

    public int nombreAbonnes(String utilisateurId) {
        return abonnementRepository.countByAbonnementTrackingId(utilisateurId);
    }

    public boolean estAbonne(String abonneId, String abonnementId) {
        return abonnementRepository.existsByAbonneTrackingIdAndAbonnementTrackingId(abonneId, abonnementId);
    }

    // ─── Utils ───────────────────────────────────────────────────────────

    private PublicationResponse toPublicationResponse(PublicationReseau p, String currentUserId) {
        return new PublicationResponse(
            p.getTrackingId(), p.getAuteurTrackingId(), p.getAuteurNom(),
            p.getAuteurRole(), p.getContenu(), p.getTypePublication(),
            p.getTags(), p.getNombreReactions(), p.getNombreCommentaires(),
            currentUserId != null && currentUserId.equals(p.getAuteurTrackingId()),
            p.getCreatedAt()
        );
    }

    private CommentaireResponse toCommentaireResponse(CommentaireReseau c) {
        return new CommentaireResponse(
            c.getTrackingId(), c.getPublicationTrackingId(),
            c.getAuteurTrackingId(), c.getAuteurNom(), c.getContenu(), c.getCreatedAt()
        );
    }
}
