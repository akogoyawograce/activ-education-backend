package tg.edtch.activEducation.profil.domain.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.domain.entite.ConsentementParental;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.ConsentementParentalRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.time.LocalDate;
import java.time.Period;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentementParentalService {

    private final ConsentementParentalRepository consentementRepository;
    private final EleveRepository eleveRepository;

    private static final int AGE_MINIMUM = 15;

    public boolean necessiteConsentement(LocalDate dateNaissance) {
        if (dateNaissance == null) return false;
        int age = Period.between(dateNaissance, LocalDate.now()).getYears();
        return age < AGE_MINIMUM;
    }

    @Transactional
    public ConsentementParental demanderConsentement(Long eleveId, String emailParent) {
        Eleve eleve = eleveRepository.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève introuvable"));

        String token = UUID.randomUUID().toString();

        ConsentementParental consentement = consentementRepository.findByEleveId(eleveId)
                .orElse(ConsentementParental.builder().eleveId(eleveId).build());

        consentement.setEmailParent(emailParent);
        consentement.setTokenValidation(token);
        consentement.setConsenti(false);
        consentement.setDateDemande(LocalDateTime.now());
        consentement = consentementRepository.save(consentement);

        log.info("Consentement parental demandé pour eleveId={}, email={}, token={}",
                eleveId, emailParent, token);

        return consentement;
    }

    @Transactional
    public boolean validerConsentement(String token, String ip) {
        Optional<ConsentementParental> opt = consentementRepository.findByTokenValidation(token);
        if (opt.isEmpty()) return false;

        ConsentementParental consentement = opt.get();
        consentement.setConsenti(true);
        consentement.setDateValidation(LocalDateTime.now());
        consentement.setIpValidation(ip);
        consentement.setTokenValidation(null);
        consentementRepository.save(consentement);

        log.info("Consentement parental validé pour eleveId={}", consentement.getEleveId());
        return true;
    }

    public boolean estConsenti(Long eleveId) {
        return consentementRepository.existsByEleveIdAndConsentiTrue(eleveId);
    }
}
