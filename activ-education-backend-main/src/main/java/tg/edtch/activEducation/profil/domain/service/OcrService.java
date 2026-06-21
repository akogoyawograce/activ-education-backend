package tg.edtch.activEducation.profil.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final GeminiEmbeddingService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> MATIERES_CONNUES = List.of(
            "Mathématiques", "Maths", "Français", "Francais", "Anglais",
            "Histoire-Géographie", "Histoire", "Géographie", "SVT",
            "Physique-Chimie", "Physique", "Chimie", "Philosophie",
            "EPS", "Sport", "Allemand", "Espagnol", "Latin", "Grec",
            "SES", "Sciences économiques et sociales",
            "Enseignement scientifique", "Numérique et sciences informatiques",
            "Arts plastiques", "Musique", "Education musicale");

    public List<NoteExtraite> extraireNotes(MultipartFile file) {
        try {
            String mimeType = file.getContentType();
            if (mimeType == null) mimeType = "application/octet-stream";

            String texteBrut;
            if (mimeType.contains("pdf")) {
                texteBrut = extraireTextePdf(file.getBytes());
                return parserNotesDepuisTexte(texteBrut);
            } else {
                texteBrut = geminiService.extractTextFromImage(file.getBytes(), mimeType);
                return parserNotesDepuisJson(texteBrut);
            }
        } catch (Exception e) {
            log.error("Erreur OCR", e);
            return List.of();
        }
    }

    String extraireTextePdf(byte[] pdfBytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    List<NoteExtraite> parserNotesDepuisTexte(String texte) {
        List<NoteExtraite> notes = new ArrayList<>();
        if (texte == null || texte.isBlank()) return notes;

        String[] lignes = texte.split("\n");
        Pattern notePattern = Pattern.compile("(\\d{1,2}(?:[.,]\\d+)?)\\s*/\\s*20");
        Pattern coeffPattern = Pattern.compile("coeff\\s*[:\\(]?\\s*(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE);

        for (String ligne : lignes) {
            String trimmed = ligne.trim();
            if (trimmed.isEmpty()) continue;

            for (String matiere : MATIERES_CONNUES) {
                if (!trimmed.toLowerCase().contains(matiere.toLowerCase())) continue;

                Matcher noteMatcher = notePattern.matcher(trimmed);
                if (noteMatcher.find()) {
                    double note = Double.parseDouble(noteMatcher.group(1).replace(",", "."));
                    double coefficient = 1.0;
                    Matcher coeffMatcher = coeffPattern.matcher(trimmed);
                    if (coeffMatcher.find()) {
                        coefficient = Double.parseDouble(coeffMatcher.group(1).replace(",", "."));
                    }
                    notes.add(new NoteExtraite(matiere, note, coefficient));
                }
                break;
            }
        }
        return notes;
    }

    List<NoteExtraite> parserNotesDepuisJson(String jsonStr) {
        List<NoteExtraite> notes = new ArrayList<>();
        if (jsonStr == null || jsonStr.isBlank()) return notes;

        String json = jsonStr.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7, json.lastIndexOf("```")).trim();
        } else if (json.startsWith("```")) {
            json = json.substring(3, json.lastIndexOf("```")).trim();
        }

        try {
            List<Map<String, Object>> items = objectMapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Object> item : items) {
                String matiere = (String) item.getOrDefault("matiere", "Inconnue");
                double note = ((Number) item.getOrDefault("note", 0)).doubleValue();
                double coefficient = item.containsKey("coefficient")
                        ? ((Number) item.get("coefficient")).doubleValue()
                        : 1.0;
                notes.add(new NoteExtraite(matiere, note, coefficient));
            }
        } catch (Exception e) {
            log.warn("Impossible de parser le JSON Gemini, fallback texte: {}", e.getMessage());
            return parserNotesDepuisTexte(jsonStr);
        }
        return notes;
    }

    public record NoteExtraite(String matiere, double note, double coefficient) {}
}
