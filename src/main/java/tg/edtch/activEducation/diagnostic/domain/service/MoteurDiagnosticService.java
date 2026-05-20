package tg.edtch.activEducation.diagnostic.domain.service;

import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;

import java.util.UUID;

public interface MoteurDiagnosticService {

        /**
         * Analyse les réponses d'un élève à un Quiz pour générer un résultat basé sur
         * ses aspirations.
         * 
         * @param eleveTrackingId     UUID de l'élève
         * @param quizTrackingId      UUID du quiz répondu
         * @param reponsesTrackingIds Liste des UUIDs des réponses choisies par l'élève
         * @return Résultat du diagnostic
         */
        ResultatDiagnosticResponse analyserQuizAspirations(UUID eleveTrackingId, UUID quizTrackingId,
                        java.util.List<UUID> reponsesTrackingIds);

        /**
         * Analyse les notes (NoteSaisiManuel) de l'élève en les confrontant aux
         * SeuilAdmission
         * pour évaluer ses chances académiques.
         * 
         * @param eleveTrackingId UUID de l'élève
         * @return Résultat du diagnostic académique
         */
        ResultatDiagnosticResponse analyserNotesAcademiques(UUID eleveTrackingId);

        /**
         * Combine l'analyse du quiz avec les notes académiques de l'élève pour fournir
         * des recommandations pondérées et optimales.
         * 
         * @param eleveTrackingId     UUID de l'élève
         * @param quizTrackingId      UUID du quiz répondu
         * @param reponsesTrackingIds Liste des UUIDs des réponses choisies
         * @return Résultat combiné du diagnostic
         */
        ResultatDiagnosticResponse diagnosticCombine(UUID eleveTrackingId, UUID quizTrackingId,
                        java.util.List<UUID> reponsesTrackingIds);

        /**
         * Détermine la question suivante à poser en fonction de la réponse choisie.
         * 
         * @param quizTrackingId    UUID du quiz
         * @param reponseTrackingId UUID de la réponse choisie
         * @param eleveTrackingId   UUID de l'élève (pour filtrage niveau)
         * @return La prochaine question ou null si fin du quiz
         */
        tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse determinerProchaineQuestion(
                        UUID quizTrackingId, UUID reponseTrackingId, UUID eleveTrackingId);

        /**
         * Récupère la liste des questions d'un quiz adaptées au niveau de l'élève.
         * 
         * @param quizTrackingId  UUID du quiz
         * @param eleveTrackingId UUID de l'élève
         * @return Liste de questions filtrées
         */
        java.util.List<tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse> getQuestionsFiltrees(
                        UUID quizTrackingId, UUID eleveTrackingId);
}
