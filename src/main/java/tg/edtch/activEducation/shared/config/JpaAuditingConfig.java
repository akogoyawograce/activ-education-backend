package tg.edtch.activEducation.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Configuration de l'audit JPA.
 * Active les
 * annotations @CreatedBy, @LastModifiedBy, @CreatedDate, @LastModifiedDate
 * dans BaseEntity.
 *
 * NOTE : Remplacer "SYSTEM" par l'extraction du principal Spring Security
 * lorsque le module de sécurité sera intégré.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // TODO : remplacer par
        // SecurityContextHolder.getContext().getAuthentication().getName()
        return () -> Optional.of("SYSTEM");
    }
}
