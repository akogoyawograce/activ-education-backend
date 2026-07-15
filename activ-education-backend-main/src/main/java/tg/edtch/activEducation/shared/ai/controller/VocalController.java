package tg.edtch.activEducation.shared.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.shared.ai.service.VocalService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vocal")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Assistant Vocal", description = "Transcription vocale et synthèse audio pour l'orientation")
public class VocalController {

    private final VocalService vocalService;

    @PostMapping(value = "/transcrire", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcrire un message vocal en texte (Whisper)")
    public ResponseEntity<Map<String, Object>> transcrire(
            @RequestParam("file") MultipartFile file) {
        String texte = vocalService.transcrire(file);
        return ResponseEntity.ok(Map.of(
                "success", !texte.startsWith("Je n'ai pas pu") && !texte.startsWith("L'assistant vocal"),
                "texte", texte));
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envoyer un message vocal à ORIA et obtenir une réponse audio")
    public ResponseEntity<Map<String, Object>> chatVoix(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        VocalService.VocalChatResult resultat = vocalService.chatVoix(file, sessionId, userId);

        if (resultat.reponseAudio() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", true,
                            "texteTranscrit", resultat.texteTranscrit(),
                            "sessionId", resultat.sessionId() != null ? resultat.sessionId() : "",
                            "reponseTexte", resultat.reponseTexte() != null ? resultat.reponseTexte() : "",
                            "reponseAudio", java.util.Base64.getEncoder().encodeToString(resultat.reponseAudio()),
                            "format", "mp3"));
        }

        return ResponseEntity.ok(Map.of(
                "success", resultat.reponseTexte() != null,
                "texteTranscrit", resultat.texteTranscrit(),
                "sessionId", resultat.sessionId() != null ? resultat.sessionId() : "",
                "reponseTexte", resultat.reponseTexte() != null ? resultat.reponseTexte() : ""));
    }

    @PostMapping(value = "/synthese", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Synthétiser un texte en audio (TTS)")
    public ResponseEntity<Map<String, Object>> synthese(@RequestBody Map<String, String> request) {
        String texte = request.get("texte");
        if (texte == null || texte.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le champ 'texte' est requis"));
        }
        byte[] audio = vocalService.syntheseVocale(texte);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "audio", java.util.Base64.getEncoder().encodeToString(audio),
                "format", "mp3"));
    }
}
