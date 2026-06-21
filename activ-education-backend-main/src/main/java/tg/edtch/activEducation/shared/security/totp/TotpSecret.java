package tg.edtch.activEducation.shared.security.totp;

import jakarta.persistence.*;
import lombok.*;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "totp_secrets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotpSecret extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false, unique = true)
    private Long utilisateurId;

    @Column(name = "secret_key", nullable = false, length = 64)
    private String secretKey;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "verifie", nullable = false)
    private boolean verifie;

    @Column(name = "active_le")
    private LocalDateTime activeLe;

    @Column(name = "dernier_usage")
    private LocalDateTime dernierUsage;
}
