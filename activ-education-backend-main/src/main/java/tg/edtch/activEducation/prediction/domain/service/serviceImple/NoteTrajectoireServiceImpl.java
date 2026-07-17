package tg.edtch.activEducation.prediction.domain.service.serviceImple;

import org.springframework.stereotype.Service;
import tg.edtch.activEducation.prediction.domain.service.NoteTrajectoireService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implémentation du calcul de trajectoire par régression linéaire simple.
 *
 * <p>Si on a 3 points : ajustement linéaire classique (moindres carrés).
 * Si 2 points : pente = (note_n - note_n-1), pas de notion de confiance accrue.
 * Si 1 point : pas de projection, on renvoie la note actuelle telle quelle,
 * confiance 0.5 (incertitude maximale).</p>
 */
@Service
public class NoteTrajectoireServiceImpl implements NoteTrajectoireService {

    @Override
    public Trajectoire calculer(List<BigDecimal> notesTriéesCroissant) {
        if (notesTriéesCroissant == null || notesTriéesCroissant.isEmpty()) {
            return new Trajectoire(null, null, BigDecimal.ZERO, 0, 0.0);
        }

        int n = notesTriéesCroissant.size();
        BigDecimal noteActuelle = notesTriéesCroissant.get(n - 1);

        if (n == 1) {
            // Pas de projection possible
            return new Trajectoire(noteActuelle, noteActuelle, BigDecimal.ZERO, 1, 0.5);
        }

        if (n == 2) {
            // Pente simple sur 2 points
            BigDecimal n0 = notesTriéesCroissant.get(0);
            BigDecimal n1 = notesTriéesCroissant.get(1);
            BigDecimal pente = n1.subtract(n0);
            BigDecimal extrapolée = n1.add(pente);
            return new Trajectoire(noteActuelle, extrapolée, pente, 2, 0.7);
        }

        // n >= 3 : régression linéaire (X = indices 0, 1, 2)
        double[] x = {0, 1, 2};
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            y[i] = notesTriéesCroissant.get(i).doubleValue();
        }

        double xMean = moyenne(x);
        double yMean = moyenne(y);

        double num = 0, den = 0;
        for (int i = 0; i < n; i++) {
            num += (x[i] - xMean) * (y[i] - yMean);
            den += (x[i] - xMean) * (x[i] - xMean);
        }
        double pente = den == 0 ? 0 : num / den;
        double intercept = yMean - pente * xMean;

        // Projection à X = 3 (l'année prochaine)
        double extrapolée = intercept + pente * 3;

        // Confiance : 1.0 si 3+ points, 0.7 si 2, 0.5 si 1, dégradé sinon
        double confiance = n >= 3 ? 1.0 : (n == 2 ? 0.7 : 0.5);

        return new Trajectoire(
                noteActuelle,
                BigDecimal.valueOf(clamp(extrapolée, 0, 20)).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(pente).setScale(3, RoundingMode.HALF_UP),
                n,
                confiance
        );
    }

    private static double moyenne(double[] valeurs) {
        double s = 0;
        for (double v : valeurs) s += v;
        return s / valeurs.length;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
