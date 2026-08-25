package tg.edtch.activEducation.shared.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Envoi d'emails transactionnels (codes OTP).
 * <p>
 * Le corps du message est un template HTML (Thymeleaf) personnalisé :
 * logo Activ Education (URL configurable, ex. Supabase Storage) et
 * coordonnées en pied de page.
 * <p>
 * Lorsque le SMTP n'est pas configuré (dev sans identifiants), l'envoi échoue
 * et le code est simplement loggé — le flux reste utilisable en développement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@activeducation.tg}")
    private String from;

    @Value("${app.mail.subject.otp:Activ Education — Votre code de vérification}")
    private String otpSubject;

    /** URL publique du logo (ex. Supabase Storage). Vide = texte à la place de l'image. */
    @Value("${app.mail.logo-url:}")
    private String logoUrl;

    /** Numéro de téléphone affiché en pied de page (format local, sans +228). */
    @Value("${app.mail.phone:}")
    private String phone;

    /**
     * Envoie un code OTP à un destinataire. En cas d'échec SMTP (ou SMTP non
     * configuré), le code est loggé comme repli de développement.
     */
    public void envoyerCodeOtp(String destinataire, String code, String contexte) {
        String subject = otpSubject + " (" + contexte + ")";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinataire);
            helper.setSubject(subject);

            Context ctx = new Context();
            ctx.setVariable("code", code);
            ctx.setVariable("contexte", contexte);
            ctx.setVariable("logoUrl", logoUrl);
            ctx.setVariable("phone", phone);
            helper.setText(templateEngine.process("email/otp", ctx), true);

            mailSender.send(message);
            log.info("OTP envoyé par email à {} (contexte : {})", destinataire, contexte);
        } catch (Exception e) {
            log.warn("Envoi d'email impossible pour {} — SMTP indisponible ou non configuré. "
                    + "Code (repli dev) : {}", destinataire, code, e);
        }
    }
}