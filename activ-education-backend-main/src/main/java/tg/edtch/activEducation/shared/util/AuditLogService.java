package tg.edtch.activEducation.shared.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String utilisateurEmail, String utilisateurNom,
                    String action, String ressource, String details,
                    String ip, String userAgent) {
        AuditLog log = AuditLog.builder()
                .utilisateurEmail(utilisateurEmail)
                .utilisateurNom(utilisateurNom)
                .action(action)
                .ressource(ressource)
                .details(details)
                .ip(ip)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogs(String email, String action,
                                   LocalDateTime fromDate, LocalDateTime toDate,
                                   Pageable pageable) {
        return auditLogRepository.findByFilters(
                email, action, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public long countByAction(String action) {
        return auditLogRepository.countByAction(action);
    }
}
