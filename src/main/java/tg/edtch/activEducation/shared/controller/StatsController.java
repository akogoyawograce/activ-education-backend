package tg.edtch.activEducation.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.profil.repository.ConseillerRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin : Stats", description = "Indicateurs clés pour le dashboard backoffice")
public class StatsController {

    private final EleveRepository eleveRepository;
    private final ConseillerRepository conseillerRepository;
    private final QuizRepository quizRepository;

    @GetMapping("/kpi")
    @Operation(summary = "Indicateurs clés (total élèves, conseillers, quiz)")
    public ResponseEntity<Map<String, Long>> getKPIs() {
        return ResponseEntity.ok(Map.of(
                "totalEleves", eleveRepository.count(),
                "totalConseillers", conseillerRepository.count(),
                "totalQuiz", quizRepository.count()));
    }
}
