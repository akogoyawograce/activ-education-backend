package tg.edtch.activEducation.prediction.application.service.serviceImple;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.prediction.application.dto.PredictionDatasetRow;
import tg.edtch.activEducation.prediction.application.service.PredictionDatasetService;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;
import tg.edtch.activEducation.prediction.domain.repository.OrientationOutcomeRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implémentation de l'export dataset pour l'entraînement supervisé.
 *
 * <p>Volontairement simple : on itère sur les outcomes fermés, on extrait
 * les features via la map JSONB (RIASEC + notes) avec une extraction
 * tolérante (on ne plante pas si une dimension manque).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PredictionDatasetServiceImpl implements PredictionDatasetService {

    private final OrientationOutcomeRepository repository;

    @Override
    public List<PredictionDatasetRow> construireDataset() {
        List<OrientationOutcome> outcomes = repository.findByStatutIn(
                List.of(OrientationOutcome.StatutOrientation.ADMIS,
                        OrientationOutcome.StatutOrientation.RECALE));

        List<PredictionDatasetRow> rows = new ArrayList<>(outcomes.size());
        for (OrientationOutcome o : outcomes) {
            rows.add(toRow(o));
        }
        log.info("Dataset d'entraînement construit : {} lignes", rows.size());
        return rows;
    }

    @Override
    public String serialiserCsv(List<PredictionDatasetRow> rows) {
        StringBuilder sb = new StringBuilder(256 + rows.size() * 128);
        // Header RFC 4180
        sb.append("row_id,niveau,serie,riasec_top3,riasec_score,")
          .append("note_actuelle,note_n1,note_n2,tendance_notes,")
          .append("score_aspiration,score_realite,score_engagement,score_recommandation,label")
          .append('\n');

        for (PredictionDatasetRow r : rows) {
            sb.append(csv(r.getRowId())).append(',')
              .append(csv(r.getNiveau())).append(',')
              .append(csv(r.getSerie())).append(',')
              .append(csv(r.getRiasecTop3())).append(',')
              .append(num(r.getRiasecScore())).append(',')
              .append(num(r.getNoteActuelle())).append(',')
              .append(num(r.getNoteN1())).append(',')
              .append(num(r.getNoteN2())).append(',')
              .append(r.getTendanceNotes() == null ? "" : r.getTendanceNotes()).append(',')
              .append(num(r.getScoreAspiration())).append(',')
              .append(num(r.getScoreRealite())).append(',')
              .append(num(r.getScoreEngagement())).append(',')
              .append(num(r.getScoreRecommandation())).append(',')
              .append(r.getLabel() == null ? "" : r.getLabel())
              .append('\n');
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Privé
    // ─────────────────────────────────────────────────────────────────────

    private PredictionDatasetRow toRow(OrientationOutcome o) {
        JsonNode riasec = o.getRiasecSnapshot();
        JsonNode notes  = o.getNotesSnapshot();

        // RIASEC : top 3 dimensions + score moyen
        String top3 = top3Riasec(riasec);
        BigDecimal riasecScore = moyenneRiasec(riasec);

        // Notes : n2, n1, actuelle
        BigDecimal nAct = decimalField(notes, "actuelle");
        BigDecimal n1    = decimalField(notes, "n1");
        BigDecimal n2    = decimalField(notes, "n2");
        Integer tendance = integerField(notes, "tendance");

        return PredictionDatasetRow.builder()
                .rowId(anonymiser(o.getTrackingId()))
                .niveau(riasec != null && riasec.hasNonNull("niveau") ? riasec.get("niveau").asText() : null)
                .serie(o.getSerie())
                .riasecTop3(top3)
                .riasecScore(riasecScore)
                .noteActuelle(nAct)
                .noteN1(n1)
                .noteN2(n2)
                .tendanceNotes(tendance)
                .scoreAspiration(o.getScoreAspiration())
                .scoreRealite(o.getScoreRealite())
                .scoreEngagement(o.getScoreEngagement())
                .scoreRecommandation(o.getScoreRecommandation())
                .label(labelFor(o.getStatut()))
                .build();
    }

    private static String top3Riasec(JsonNode riasec) {
        if (riasec == null || !riasec.isObject()) return "";
        // Tri stable par score DESC puis par clé ASC
        TreeMap<String, BigDecimal> sorted = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = riasec.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> e = fields.next();
            if ("niveau".equals(e.getKey())) continue;
            if (e.getValue().isNumber()) {
                sorted.put(e.getKey(), e.getValue().decimalValue());
            }
        }
        return sorted.descendingMap().entrySet().stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .reduce((a, b) -> a + "|" + b)
                .orElse("");
    }

    private static BigDecimal moyenneRiasec(JsonNode riasec) {
        if (riasec == null || !riasec.isObject()) return null;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        Iterator<Map.Entry<String, JsonNode>> fields = riasec.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> e = fields.next();
            if ("niveau".equals(e.getKey())) continue;
            if (e.getValue().isNumber()) {
                sum = sum.add(e.getValue().decimalValue());
                count++;
            }
        }
        return count == 0 ? null : sum.divide(BigDecimal.valueOf(count), 3, java.math.RoundingMode.HALF_UP);
    }

    private static BigDecimal decimalField(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) return null;
        JsonNode v = node.get(field);
        return v.isNumber() ? v.decimalValue() : null;
    }

    private static Integer integerField(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) return null;
        JsonNode v = node.get(field);
        return v.isInt() ? v.intValue() : null;
    }

    private static int labelFor(OrientationOutcome.StatutOrientation s) {
        return s == OrientationOutcome.StatutOrientation.ADMIS ? 1 : 0;
    }

    /**
     * SHA-256 du trackingId, retourné en hexadécimal tronqué à 16 caractères
     * (64 bits de sécurité, largement suffisant pour l'anonymisation).
     */
    private static String anonymiser(java.util.UUID trackingId) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(trackingId.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti disponible en JVM, mais on reste défensif
            return Long.toHexString(trackingId.getLeastSignificantBits());
        }
    }

    /** RFC 4180 quoting : entoure de "" si la valeur contient , " ou newline. */
    private static String csv(String v) {
        if (v == null) return "";
        if (v.indexOf(',') < 0 && v.indexOf('"') < 0 && v.indexOf('\n') < 0) {
            return v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private static String num(BigDecimal v) {
        return v == null ? "" : v.toPlainString();
    }
}
