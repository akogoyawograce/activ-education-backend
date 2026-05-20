package tg.edtch.activEducation.diagnostic.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;
import tg.edtch.activEducation.diagnostic.domain.service.MoteurDiagnosticService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagnostic/moteur")
@RequiredArgsConstructor
@Tag(name = "Diagnostic : Moteur Intelligent", description = "Endpoints de génération automatique de recommandations (Aspirations, Académique, Combiné)")
public class MoteurDiagnosticController {

    private final MoteurDiagnosticService moteurDiagnosticService;

    @PostMapping("/quiz/{eleveTrackingId}/{quizTrackingId}")
    @Operation(summary = "Générer un diagnostic basé sur les réponses à un quiz (Aspirations)")
    public ResponseEntity<ResultatDiagnosticResponse> analyserQuizAspirations(
            @PathVariable UUID eleveTrackingId,
            @PathVariable UUID quizTrackingId,
            @RequestBody List<UUID> reponsesTrackingIds) {

        ResultatDiagnosticResponse response = moteurDiagnosticService.analyserQuizAspirations(eleveTrackingId,
                quizTrackingId, reponsesTrackingIds);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/academique/{eleveTrackingId}")
    @Operation(summary = "Générer un diagnostic basé sur les notes saisies (Académique)")
    public ResponseEntity<ResultatDiagnosticResponse> analyserNotesAcademiques(
            @PathVariable UUID eleveTrackingId) {

        ResultatDiagnosticResponse response = moteurDiagnosticService.analyserNotesAcademiques(eleveTrackingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/combine/{eleveTrackingId}/{quizTrackingId}")
    @Operation(summary = "Générer un diagnostic croisant aspirations et notes académiques")
    public ResponseEntity<ResultatDiagnosticResponse> diagnosticCombine(
            @PathVariable UUID eleveTrackingId,
            @PathVariable UUID quizTrackingId,
            @RequestBody List<UUID> reponsesTrackingIds) {

        ResultatDiagnosticResponse response = moteurDiagnosticService.diagnosticCombine(eleveTrackingId, quizTrackingId,
                reponsesTrackingIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prochaine-question/{quizTrackingId}/{reponseTrackingId}/{eleveTrackingId}")
    @Operation(summary = "Calculer la prochaine question à poser (branchement ou ordre naturel)")
    public ResponseEntity<tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse> determinerProchaineQuestion(
            @PathVariable UUID quizTrackingId,
            @PathVariable UUID reponseTrackingId,
            @PathVariable UUID eleveTrackingId) {

        tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse response = moteurDiagnosticService
                .determinerProchaineQuestion(quizTrackingId, reponseTrackingId, eleveTrackingId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/questions-filtrees/{quizTrackingId}/{eleveTrackingId}")
    @Operation(summary = "Récupérer toutes les questions d'un quiz adaptées au niveau de l'élève")
    public ResponseEntity<List<tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse>> getQuestionsFiltrees(
            @PathVariable UUID quizTrackingId,
            @PathVariable UUID eleveTrackingId) {

        List<tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse> response = moteurDiagnosticService
                .getQuestionsFiltrees(quizTrackingId, eleveTrackingId);
        return ResponseEntity.ok(response);
    }
}
