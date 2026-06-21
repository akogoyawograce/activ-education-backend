package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FavoriRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FavoriResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FavoriMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.Favori;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;
import tg.edtch.activEducation.bibliotheque.domain.service.FavoriService;
import tg.edtch.activEducation.bibliotheque.repository.FavoriRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FavoriServiceImpl implements FavoriService {

    private final FavoriRepository favoriRepository;
    private final FicheRepository ficheRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FavoriMapper favoriMapper;

    @Override
    public FavoriResponse ajouterFavori(FavoriRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByTrackingId(request.getUtilisateurTrackingId())
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));

        Fiche fiche = ficheRepository.findByTrackingId(request.getFicheTrackingId())
                .orElseThrow(() -> new NoSuchElementException("Fiche introuvable"));

        // Vérifier si déjà en favori
        if (favoriRepository.findByUtilisateurIdAndFicheId(utilisateur.getId(), fiche.getId()).isPresent()) {
            throw new IllegalArgumentException("Cette fiche est déjà dans les favoris de l'utilisateur.");
        }

        Favori favori = Favori.builder()
                .utilisateur(utilisateur)
                .fiche(fiche)
                .notePersonnelle(request.getNotePersonnelle())
                .build();

        Favori saved = favoriRepository.save(favori);
        log.info("Favori ajouté : trackingId={}", saved.getTrackingId());
        return favoriMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriResponse getFavori(UUID trackingId) {
        return favoriMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriResponse> listerParUtilisateur(UUID utilisateurTrackingId, Pageable pageable) {
        Utilisateur utilisateur = utilisateurRepository.findByTrackingId(utilisateurTrackingId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));
        return favoriRepository.findByUtilisateurId(utilisateur.getId(), pageable)
                .map(favoriMapper::toResponse);
    }

    @Override
    public void supprimerFavori(UUID trackingId) {
        Favori favori = findOrThrow(trackingId);
        favoriRepository.delete(favori);
        log.info("Favori supprimé : trackingId={}", trackingId);
    }

    private Favori findOrThrow(UUID trackingId) {
        return favoriRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException("Favori introuvable pour le trackingId : " + trackingId));
    }
}
