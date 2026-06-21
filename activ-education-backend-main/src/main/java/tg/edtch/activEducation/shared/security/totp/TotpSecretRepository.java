package tg.edtch.activEducation.shared.security.totp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotpSecretRepository extends JpaRepository<TotpSecret, Long> {
    Optional<TotpSecret> findByUtilisateurId(Long utilisateurId);
    boolean existsByUtilisateurIdAndActifTrue(Long utilisateurId);
    void deleteByUtilisateurId(Long utilisateurId);
}
