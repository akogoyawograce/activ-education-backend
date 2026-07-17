package tg.edtch.activEducation.prediction.domain.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Configuration externalisée du moteur de recommandation 3 signaux.
 *
 * <p>Modifiable via {@code application.properties} avec le préfixe
 * {@code app.prediction.*}. La validation est activée ({@code @Validated}) :
 * un poids hors borne lèvera une exception au démarrage, ce qui est
 * exactement ce qu'on veut pour éviter une régression silencieuse
 * (cf. le problème de "bulle de filtre" identifié en Phase 0).</p>
 *
 * <p><strong>Garde-fou engagement</strong> : {@code poidsEngagementMax}
 * plafonne la contribution du signal comportemental. La valeur effective
 * utilisée par le moteur est {@code min(poidsEngagement, poidsEngagementMax)}.
 * Si l'admin met 0.5 dans la config, le moteur utilise 0.20.</p>
 */
@Data
@Validated
@Component
@Configuration
@ConfigurationProperties(prefix = "app.prediction")
public class PredictionProperties {

    /** Poids du score_aspiration dans le score final. Défaut : 0.35. */
    @NotNull
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal poidsAspiration = new BigDecimal("0.35");

    /** Poids du score_realite dans le score final. Défaut : 0.50. */
    @NotNull
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal poidsRealite = new BigDecimal("0.50");

    /** Poids du score_engagement dans le score final. Défaut : 0.15. */
    @NotNull
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal poidsEngagement = new BigDecimal("0.15");

    /**
     * Plafond strict du poids d'engagement. Le moteur utilise
     * {@code min(poidsEngagement, poidsEngagementMax)} pour éviter
     * la bulle de filtre. Défaut : 0.20 (recommandation Phase 0).
     */
    @NotNull
    @DecimalMin("0.0") @DecimalMax("0.30")
    private BigDecimal poidsEngagementMax = new BigDecimal("0.20");

    /** Nombre de filières renvoyées dans le top. Défaut : 10. */
    @Min(1)
    private int topN = 10;

    /**
     * Nombre de filières "découverte" garanties dans le top (faible
     * engagement, fort potentiel aspiration/réalité). Défaut : 2.
     */
    @Min(0)
    private int decouvertesMin = 2;

    /**
     * Note seuil d'admission par défaut (échelle 0..20) utilisée quand
     * la filière ne précise pas son propre seuil. Défaut : 12.
     */
    @NotNull
    @DecimalMin("0.0") @DecimalMax("20.0")
    private BigDecimal seuilAdmissionDefaut = new BigDecimal("12.0");

    /**
     * Renvoie le poids effectif d'engagement après application du plafond.
     */
    public BigDecimal poidsEngagementEffectif() {
        return poidsEngagement.min(poidsEngagementMax);
    }
}
