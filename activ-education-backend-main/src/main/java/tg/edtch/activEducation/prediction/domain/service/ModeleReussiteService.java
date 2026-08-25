package tg.edtch.activEducation.prediction.domain.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.prediction.application.dto.ProfilEleve;
import tg.edtch.activEducation.prediction.domain.config.PredictionProperties;
import tg.edtch.activEducation.prediction.domain.util.ProfilFiliereRiasecCatalog;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Prédiction de réussite (probabilité d'ADMIS) par filière via le modèle
 * GradientBoosting Phase 5 exporté en ONNX
 * ({@code models/gb_sans_comportemental.onnx}).
 *
 * <p>Le modèle a été entraîné sur 6 000 parcours calibrés (seed 42, taux
 * BAC réels 2023/2024 par région × série × sexe). L'export ONNX est
 * strictement équivalent au joblib (max diff &lt; 1e-7 sur 300 lignes).
 * Les colonnes catégorielles absentes du profil (region, ordre, sexe,
 * serie) sont neutralisées par {@code INCONNU} (one-hot à zéro,
 * {@code handle_unknown="ignore"}).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModeleReussiteService {

    private static final List<String> DIMENSIONS = List.of("R", "I", "A", "S", "E", "C");
    private static final String MODELE_PATH = "models/gb_sans_comportemental.onnx";
    private static final int ANNEE_SESSION = 2024;

    private final PredictionProperties properties;

    private OrtEnvironment env;
    private OrtSession session;

    @PostConstruct
    void init() {
        try {
            log.info("[MODELE-REUSSITE] Chargement du moteur ONNX…");
            env = OrtEnvironment.getEnvironment();
            log.info("[MODELE-REUSSITE] OrtEnvironment OK, création de la session…");
            try (var in = new ClassPathResource(MODELE_PATH).getInputStream()) {
                session = env.createSession(in.readAllBytes(), new OrtSession.SessionOptions());
            }
            log.info("[MODELE-REUSSITE] Modèle ONNX chargé : {} (entrées={})",
                    MODELE_PATH, session.getInputNames());
        } catch (IOException | OrtException e) {
            log.warn("[MODELE-REUSSITE] Modèle ONNX indisponible ({}) : la proba ML est désactivée, "
                    + "le moteur 3 signaux fonctionne sans elle.", e.getMessage());
            session = null;
        }
    }

    @PreDestroy
    void close() {
        if (session != null) {
            try {
                session.close();
            } catch (OrtException ignored) {
                // fermeture best effort
            }
            session = null;
        }
    }

    /**
     * Probabilité d'ADMIS de l'élève dans la filière, selon le modèle Phase 5.
     *
     * @param profil         profil élève (RIASEC, notes, niveau)
     * @param fiche          filière cible
     * @param notesCroissant 3 dernières moyennes générales (ordre croissant)
     * @return probabilité en 0..1, ou 0.5 si le modèle est indisponible
     */
    public double probabiliteAdmission(ProfilEleve profil, FicheFiliere fiche,
                                       List<BigDecimal> notesCroissant) {
        if (session == null) {
            return 0.5;
        }
        try {
            double[] riasec = riasecOneHot(profil);
            double[] notes = troisNotes(notesCroissant);
            double noteN2 = notes[0], noteN1 = notes[1], noteActuelle = notes[2];
            double tendance = tendanceLineaire(noteN2, noteN1, noteActuelle);
            double moyenne = (noteN2 + noteN1 + noteActuelle) / 3.0;

            double[] catalogue = ProfilFiliereRiasecCatalog.profilPour(fiche.getTitre());
            double aspiration = cosinus(profil.getRiasec(), catalogue);
            double realite = Math.max(0.0, Math.min(1.0, (moyenne - seuil() + 5.0) / 10.0));
            double score60_40 = 0.6 * realite + 0.4 * aspiration;
            double ecartNotesSeuil = (noteActuelle - 10.0) / 10.0;

            double[][] floats = new double[15][1];
            floats[0][0] = riasec[0];
            floats[1][0] = riasec[1];
            floats[2][0] = riasec[2];
            floats[3][0] = riasec[3];
            floats[4][0] = riasec[4];
            floats[5][0] = riasec[5];
            floats[6][0] = noteN2;
            floats[7][0] = noteN1;
            floats[8][0] = noteActuelle;
            floats[9][0] = tendance;
            floats[10][0] = moyenne;
            floats[11][0] = score60_40;
            floats[12][0] = aspiration;
            floats[13][0] = ecartNotesSeuil;
            floats[14][0] = ANNEE_SESSION;

            String[][] strings = new String[6][1];
            strings[0][0] = "INCONNU";                 // serie
            strings[1][0] = fiche.getTitre();          // filiere_choisie
            strings[2][0] = profil.getNiveau() != null ? profil.getNiveau() : "INCONNU";
            strings[3][0] = "INCONNU";                 // region
            strings[4][0] = "INCONNU";                 // ordre
            strings[5][0] = "INCONNU";                 // sexe

            try (OnnxTensor t0 = tensor(floats[0]); OnnxTensor t1 = tensor(floats[1]);
                 OnnxTensor t2 = tensor(floats[2]); OnnxTensor t3 = tensor(floats[3]);
                 OnnxTensor t4 = tensor(floats[4]); OnnxTensor t5 = tensor(floats[5]);
                 OnnxTensor t6 = tensor(floats[6]); OnnxTensor t7 = tensor(floats[7]);
                 OnnxTensor t8 = tensor(floats[8]); OnnxTensor t9 = tensor(floats[9]);
                 OnnxTensor t10 = tensor(floats[10]); OnnxTensor t11 = tensor(floats[11]);
                 OnnxTensor t12 = tensor(floats[12]); OnnxTensor t13 = tensor(floats[13]);
                 OnnxTensor t14 = tensor(floats[14]);
                 OnnxTensor s0 = OnnxTensor.createTensor(env, new String[][]{{strings[0][0]}});
                 OnnxTensor s1 = OnnxTensor.createTensor(env, new String[][]{{strings[1][0]}});
                 OnnxTensor s2 = OnnxTensor.createTensor(env, new String[][]{{strings[2][0]}});
                 OnnxTensor s3 = OnnxTensor.createTensor(env, new String[][]{{strings[3][0]}});
                 OnnxTensor s4 = OnnxTensor.createTensor(env, new String[][]{{strings[4][0]}});
                 OnnxTensor s5 = OnnxTensor.createTensor(env, new String[][]{{strings[5][0]}});
                 ai.onnxruntime.OrtSession.Result result = session.run(java.util.Map.ofEntries(
                         java.util.Map.entry("riasec_R", t0), java.util.Map.entry("riasec_I", t1),
                         java.util.Map.entry("riasec_A", t2), java.util.Map.entry("riasec_S", t3),
                         java.util.Map.entry("riasec_E", t4), java.util.Map.entry("riasec_C", t5),
                         java.util.Map.entry("notes_n2", t6), java.util.Map.entry("notes_n1", t7),
                         java.util.Map.entry("notes_actuelle", t8), java.util.Map.entry("tendance_notes", t9),
                         java.util.Map.entry("moyenne_generale", t10), java.util.Map.entry("score_60_40", t11),
                         java.util.Map.entry("match_riasec", t12), java.util.Map.entry("ecart_notes_seuil", t13),
                         java.util.Map.entry("annee_session", t14), java.util.Map.entry("serie", s0),
                         java.util.Map.entry("filiere_choisie", s1), java.util.Map.entry("niveau_actuel", s2),
                         java.util.Map.entry("region", s3), java.util.Map.entry("ordre", s4),
                         java.util.Map.entry("sexe", s5)))) {
                 float[][] out = (float[][]) result.get(1).getValue();
                return out[0][1];
            }
        } catch (OrtException e) {
            log.warn("[MODELE-REUSSITE] Échec prédiction {} / {} : {}", fiche.getTitre(),
                    profil.getTrackingId(), e.getMessage());
            return 0.5;
        }
    }

    /** Seuil d'admission par défaut (identique au moteur 3 signaux). */
    private double seuil() {
        BigDecimal s = properties.getSeuilAdmissionDefaut();
        return s != null ? s.doubleValue() : 10.0;
    }

    /** One-hot des 3 lettres dominantes du code RIASEC (comme à l'entraînement). */
    private double[] riasecOneHot(ProfilEleve profil) {
        String code = profil.getProfilDecouvert();
        double[] v = new double[6];
        if (code == null) {
            return v;
        }
        String upper = code.toUpperCase(Locale.ROOT);
        for (int i = 0; i < 6; i++) {
            v[i] = upper.indexOf(DIMENSIONS.get(i)) >= 0 ? 1.0 : 0.0;
        }
        return v;
    }

    /** Dernières 3 moyennes croissantes ; complète par 10.0 si manquantes. */
    private double[] troisNotes(List<BigDecimal> notesCroissant) {
        double[] out = new double[3];
        for (int i = 0; i < 3; i++) {
            int idx = notesCroissant.size() - 3 + i;
            out[i] = idx >= 0 && notesCroissant.get(idx) != null
                    ? notesCroissant.get(idx).doubleValue() : 10.0;
        }
        return out;
    }

    /** Pente de régression linéaire sur x = [0,1,2] (même formule que le générateur). */
    static double tendanceLineaire(double n2, double n1, double actuelle) {
        return (actuelle - n2) / 2.0;
    }

    private OnnxTensor tensor(double[] v) throws OrtException {
        return OnnxTensor.createTensor(env, new double[][]{v});
    }

    private static double cosinus(List<Double> a, double[] b) {
        if (a == null || b == null || a.size() != 6) {
            return 0.0;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < 6; i++) {
            dot += a.get(i) * b[i];
            normA += a.get(i) * a.get(i);
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, dot / (Math.sqrt(normA) * Math.sqrt(normB))));
    }
}