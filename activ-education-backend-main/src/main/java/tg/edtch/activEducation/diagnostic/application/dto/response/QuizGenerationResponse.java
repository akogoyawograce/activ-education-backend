package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerationResponse {

    private UUID quizTrackingId;
    private String titre;
    private String description;
    private String entityType;
    private UUID entityId;
    private boolean genereParIA;
    private List<QuestionGeneree> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionGeneree {
        private String texte;
        private String typeQuestion;
        private String domaine;
        private Integer difficulte;
        private List<ReponseGeneree> reponses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReponseGeneree {
        private String texte;
        private boolean correct;
    }
}
