package tg.edtch.activEducation.shared.ai.domain.dto;

import java.time.Instant;
import java.util.List;

public class OriaResponse {
    private String message;
    private String sessionId;
    private List<MessageDto> historique;

    public OriaResponse() {}

    public OriaResponse(String message, String sessionId, List<MessageDto> historique) {
        this.message = message;
        this.sessionId = sessionId;
        this.historique = historique;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public List<MessageDto> getHistorique() { return historique; }
    public void setHistorique(List<MessageDto> historique) { this.historique = historique; }

    public static class MessageDto {
        private String role;
        private String contenu;
        private Instant timestamp;

        public MessageDto() {}

        public MessageDto(String role, String contenu, Instant timestamp) {
            this.role = role;
            this.contenu = contenu;
            this.timestamp = timestamp;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContenu() { return contenu; }
        public void setContenu(String contenu) { this.contenu = contenu; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }
}
