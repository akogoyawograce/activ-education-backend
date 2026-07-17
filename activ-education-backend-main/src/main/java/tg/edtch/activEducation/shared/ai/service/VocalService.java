package tg.edtch.activEducation.shared.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaRequest;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaResponse;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocalService {

    private final AIEmbeddingService aiService;
    private final OriaService oriaService;

    public String transcrire(MultipartFile audioFile) {
        try {
            byte[] audioData = audioFile.getBytes();
            String filename = audioFile.getOriginalFilename();
            if (filename == null || filename.isBlank()) {
                filename = "audio." + getExtension(audioFile.getContentType());
            }
            return aiService.transcribeAudio(audioData, filename);
        } catch (IOException e) {
            log.error("Erreur lecture fichier audio", e);
            return "Je n'ai pas pu lire votre message vocal.";
        }
    }

    public VocalChatResult chatVoix(MultipartFile audioFile, String sessionId, String userId) {
        String texteTranscrit = transcrire(audioFile);
        if (texteTranscrit.startsWith("Je n'ai pas pu") || texteTranscrit.startsWith("L'assistant vocal")) {
            return new VocalChatResult(texteTranscrit, null, null, null, null);
        }

        OriaRequest oriaRequest = new OriaRequest();
        oriaRequest.setMessage(texteTranscrit);
        oriaRequest.setSessionId(sessionId);

        OriaResponse oriaResponse;
        try {
            oriaResponse = oriaService.sendMessageAndPersist(oriaRequest, userId);
        } catch (Exception e) {
            log.error("Erreur ORIA après transcription", e);
            return new VocalChatResult(texteTranscrit, null, null,
                    "Désolé, je n'ai pas pu répondre après avoir transcrit votre message.", null);
        }

        byte[] audioReponse = null;
        try {
            audioReponse = aiService.generateSpeech(oriaResponse.getMessage());
        } catch (Exception e) {
            log.warn("Impossible de générer l'audio de réponse (TTS): {}", e.getMessage());
        }

        return new VocalChatResult(texteTranscrit, oriaResponse.getSessionId(),
                oriaResponse.getHistorique(), oriaResponse.getMessage(), audioReponse);
    }

    public byte[] syntheseVocale(String texte) {
        return aiService.generateSpeech(texte);
    }

    private String getExtension(String contentType) {
        if (contentType == null) return "webm";
        return switch (contentType) {
            case "audio/webm" -> "webm";
            case "audio/wav", "audio/wave" -> "wav";
            case "audio/mp3", "audio/mpeg" -> "mp3";
            case "audio/ogg" -> "ogg";
            case "audio/aac" -> "aac";
            default -> "webm";
        };
    }

    public record VocalChatResult(
            String texteTranscrit,
            String sessionId,
            List<OriaResponse.MessageDto> historique,
            String reponseTexte,
            byte[] reponseAudio
    ) {
        public VocalChatResult(String erreur) {
            this(erreur, null, null, null, null);
        }
    }
}
