package tg.edtch.activEducation.shared.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi d'emails transactionnels (codes OTP).
 * <p>
 * Lorsque le SMTP n'est pas configuré (dev sans identifiants), l'envoi échoue
 * et le code est simplement loggé — le flux reste utilisable en développement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@activeducation.tg}")
    private String from;

    @Value("${app.mail.subject.otp:Activ Education — Votre code de vérification}")
    private String otpSubject;

    /**
     * Envoie un code OTP à un destinataire. En cas d'échec SMTP (ou SMTP non
     * configuré), le code est loggé comme repli de développement.
     */
    public void envoyerCodeOtp(String destinataire, String code, String contexte) {
        String subject = otpSubject + " (" + contexte + ")";
        String corps = "Bonjour,\n\n"
                + "Utilisez le code suivant pour " + contexte + " :\n\n"
                + "    " + code + "\n\n"
                + "Ce code expire dans 5 minutes. Si vous n'êtes pas à l'origine de cette "
                + "demande, ignorez simplement cet email.\n\n"
                + "L'équipe Activ Education";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(destinataire);
            message.setSubject(subject);
            message.setText(corps);
            mailSender.send(message);
            log.info("OTP envoyé par email à {} (contexte : {})", destinataire, contexte);
        } catch (Exception e) {
            log.warn("Envoi d'email impossible pour {} — SMTP indisponible ou non configuré. "
                    + "Code (repli dev) : {}", destinataire, code, e);
        }
    }
}