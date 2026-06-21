package tg.edtch.activEducation.shared.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;
import tg.edtch.activEducation.shared.security.jwt.JwtService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null) {
            closeWithError(session, "Token manquant");
            return;
        }
        try {
            String email = jwtService.extractUsername(token);
            if (email == null || !jwtService.isTokenValid(token, null)) {
                closeWithError(session, "Token invalide");
                return;
            }
            String userId = jwtService.extractTrackingId(token).toString();
            sessions.put(userId, session);
            log.info("WebSocket connecté: {}", userId);
        } catch (Exception e) {
            closeWithError(session, "Erreur d'authentification");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage msg = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            String recipientSessionId = msg.destinataireTrackingId();
            WebSocketSession recipientSession = sessions.get(recipientSessionId);
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.sendMessage(new TextMessage(message.getPayload()));
            }
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "delivered", "messageId", msg.messageId()))));
        } catch (IOException e) {
            log.error("Erreur WebSocket", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
        log.info("WebSocket déconnecté: {}", status);
    }

    private String extractToken(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] parts = param.split("=", 2);
                if (parts.length == 2 && "token".equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    private void closeWithError(WebSocketSession session, String reason) {
        try {
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException ignored) {}
    }

    public record ChatMessage(
            String messageId,
            String expediteurTrackingId,
            String destinataireTrackingId,
            String contenu,
            long timestamp
    ) {}
}
