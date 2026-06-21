package tg.edtch.activEducation.shared.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.domain.service.OcrService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eleves/{trackingId}/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR Bulletins", description = "Reconnaissance automatique des notes sur bulletins scolaires")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@security.isOwner(#trackingId) or @security.isOwnChild(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Extraire les notes d'un bulletin par OCR")
    public ResponseEntity<Map<String, Object>> analyserBulletin(
            @PathVariable UUID trackingId,
            @RequestParam("file") MultipartFile file) {
        List<OcrService.NoteExtraite> notes = ocrService.extraireNotes(file);
        return ResponseEntity.ok(Map.of(
                "success", !notes.isEmpty(),
                "notes", notes,
                "nombre", notes.size()));
    }
}
