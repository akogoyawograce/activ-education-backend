package tg.edtch.activEducation.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class CsvController {

    private final EleveRepository eleveRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @GetMapping("/export/eleves")
    public ResponseEntity<byte[]> exportEleves() {
        List<Eleve> eleves = eleveRepository.findAll();
        String csv = toCsv(eleves, List.of(
                "email", "nom", "prenom", "telephone", "typeApprenant",
                "niveau", "etablissement", "dateInscription", "estActif"));
        return csvResponse(csv, "eleves.csv");
    }

    @PostMapping("/import/eleves")
    public ResponseEntity<Map<String, Object>> importEleves(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Map<String, String>> rows = parseCsv(content);
            int imported = 0;
            for (Map<String, String> row : rows) {
                String email = row.get("email");
                if (email == null || email.isBlank()) continue;
                if (eleveRepository.findByEmail(email).isPresent()) continue;

                String tempPass = UUID.randomUUID().toString();
                Eleve eleve = Eleve.builder()
                        .email(email)
                        .motDePasseHash(passwordEncoder.encode(tempPass))
                        .nom(row.getOrDefault("nom", ""))
                        .prenom(row.getOrDefault("prenom", ""))
                        .telephone(row.get("telephone"))
                        .estActif(true)
                        .dateInscription(LocalDateTime.now())
                        .build();
                log.info("Import CSV : utilisateur {} créé avec mot de passe temporaire (réinitialisation requise)", email);
                eleveRepository.save(eleve);
                imported++;
            }
            log.info("Import CSV terminé : {} utilisateurs importés sur {}", imported, rows.size());
            return ResponseEntity.ok(Map.of("success", true, "imported", imported, "total", rows.size()));
        } catch (Exception e) {
            log.error("Erreur import CSV", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private <T> String toCsv(List<T> entities, List<String> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", fields)).append("\n");
        for (T entity : entities) {
            List<String> values = fields.stream().map(f -> {
                try {
                    Object val = entity.getClass().getMethod("get" + Character.toUpperCase(f.charAt(0)) + f.substring(1)).invoke(entity);
                    return val != null ? escapeCsv(val.toString()) : "";
                } catch (Exception e) {
                    try {
                        Object val = entity.getClass().getMethod(f).invoke(entity);
                        return val != null ? escapeCsv(val.toString()) : "";
                    } catch (Exception e2) {
                        return "";
                    }
                }
            }).collect(Collectors.toList());
            sb.append(String.join(",", values)).append("\n");
        }
        return sb.toString();
    }

    private List<Map<String, String>> parseCsv(String content) {
        List<Map<String, String>> rows = new ArrayList<>();
        String[] lines = content.split("\n");
        if (lines.length < 2) return rows;
        String[] headers = lines[0].trim().split(",");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] values = line.split(",");
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < headers.length && j < values.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }
            rows.add(row);
        }
        return rows;
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private ResponseEntity<byte[]> csvResponse(String csv, String filename) {
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }
}
