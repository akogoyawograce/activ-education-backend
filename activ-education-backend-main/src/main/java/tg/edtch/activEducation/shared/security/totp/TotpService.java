package tg.edtch.activEducation.shared.security.totp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.shared.security.exception.InvalidTokenException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotpService {

    private final TotpSecretRepository totpSecretRepository;

    private static final int SECRET_SIZE = 20;
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int WINDOW_SIZE = 1;
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final SecureRandom RANDOM = new SecureRandom();

    public TotpSetupData generateSecret(Long utilisateurId, String email) {
        totpSecretRepository.findByUtilisateurId(utilisateurId).ifPresent(existing -> {
            if (existing.isActif()) {
                throw new IllegalStateException("TOTP déjà activé pour cet utilisateur");
            }
            totpSecretRepository.delete(existing);
        });

        byte[] secretBytes = new byte[SECRET_SIZE];
        RANDOM.nextBytes(secretBytes);

        Base32 base32 = new Base32();
        String secretKey = base32.encodeToString(secretBytes);

        TotpSecret totpSecret = TotpSecret.builder()
                .utilisateurId(utilisateurId)
                .secretKey(secretKey)
                .actif(false)
                .verifie(false)
                .build();
        totpSecretRepository.save(totpSecret);

        String issuer = "ActivEducation";
        String qrUri = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, email, secretKey, issuer, CODE_DIGITS, TIME_STEP_SECONDS
        );

        return new TotpSetupData(secretKey, qrUri);
    }

    @Transactional
    public boolean verifyAndEnable(Long utilisateurId, int code) {
        TotpSecret totpSecret = totpSecretRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new InvalidTokenException("TOTP non configuré"));

        if (totpSecret.isActif()) {
            throw new IllegalStateException("TOTP déjà activé");
        }

        if (verifyCode(totpSecret.getSecretKey(), code)) {
            totpSecret.setActif(true);
            totpSecret.setVerifie(true);
            totpSecret.setActiveLe(LocalDateTime.now());
            totpSecretRepository.save(totpSecret);
            return true;
        }
        return false;
    }

    public boolean validateCode(Long utilisateurId, int code) {
        TotpSecret totpSecret = totpSecretRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new InvalidTokenException("TOTP non configuré"));

        if (!totpSecret.isActif()) {
            throw new InvalidTokenException("TOTP n'est pas activé");
        }

        if (verifyCode(totpSecret.getSecretKey(), code)) {
            totpSecret.setDernierUsage(LocalDateTime.now());
            totpSecretRepository.save(totpSecret);
            return true;
        }
        return false;
    }

    @Transactional
    public void disable(Long utilisateurId) {
        totpSecretRepository.deleteByUtilisateurId(utilisateurId);
        log.info("TOTP désactivé pour l'utilisateur {}", utilisateurId);
    }

    public boolean isTotpEnabled(Long utilisateurId) {
        return totpSecretRepository.existsByUtilisateurIdAndActifTrue(utilisateurId);
    }

    boolean verifyCode(String secretKey, int code) {
        long timeCounter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

        for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; i++) {
            long hash = generateTOTP(secretKey, timeCounter + i);
            if (hash == code) {
                return true;
            }
        }
        return false;
    }

    int generateTOTP(String secretKey, long timeCounter) {
        Base32 base32 = new Base32();
        byte[] keyBytes = base32.decode(secretKey);

        byte[] counterBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counterBytes[i] = (byte) (timeCounter & 0xff);
            timeCounter >>= 8;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(counterBytes);

            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);

            return binary % (int) Math.pow(10, CODE_DIGITS);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erreur TOTP", e);
        }
    }

    public record TotpSetupData(String secretKey, String qrUri) {}
}
