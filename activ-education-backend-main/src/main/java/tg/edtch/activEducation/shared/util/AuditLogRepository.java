package tg.edtch.activEducation.shared.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:email IS NULL OR a.utilisateurEmail LIKE %:email%) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:fromDate IS NULL OR a.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR a.createdAt <= :toDate) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(
            @Param("email") String email,
            @Param("action") String action,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    long countByAction(String action);
}
