package tg.edtch.activEducation.shared.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaRequest;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaResponse;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaResponse.MessageDto;
import tg.edtch.activEducation.shared.ai.domain.entite.OriaMessage;
import tg.edtch.activEducation.shared.ai.repository.OriaMessageRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OriaService {

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.api.model:llama3.1:8b}")
    private String ollamaModel;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.chat.model:gpt-4o-mini}")
    private String chatModel;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    private final OriaMessageRepository messageRepository;
    private final AIEmbeddingService aiEmbeddingService;
    private final FicheRepository ficheRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_SESSIONS = 1000;
    private static final int MAX_MESSAGES_PER_SESSION = 20;

    private final ConcurrentHashMap<String, OriaSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<String> sessionOrder = new ConcurrentLinkedDeque<>();

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
        "(?i)(ignore\\s+(all\\s+)?(previous|above|below)|forget\\s+(all\\s+)?(instructions|context)|" +
        "system\\s+(prompt|message|instruction)|you\\s+are\\s+(now|not\\s+really)|" +
        "act\\s+as\\s+(if|though)|pretend\\s+(to\\s+)?be|bypass|jailbreak)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> BLOCKED_WORDS = Set.of(
        "hack", "pirate", "exploit", "vuln", "malware", "virus", "phish"
    );

    @Transactional
    public OriaResponse sendMessage(OriaRequest request, String userId) {
        String message = request.getMessage().trim();

        String validationError = validateMessage(message);
        if (validationError != null) {
            return buildErrorResponse(validationError);
        }

        String sessionId = resolveSessionId(request, userId);
        OriaSession session = getOrCreateSession(sessionId, request.getContexteOrientation());

        session.messages.add(new ChatMessage("user", message, Instant.now()));
        if (session.messages.size() > MAX_MESSAGES_PER_SESSION) {
            session.messages.remove(0);
        }

        try {
            var contexte = rechercherContexte(message);
            String response = callLLM(session, contexte);
            session.messages.add(new ChatMessage("assistant", response, Instant.now()));
            if (session.messages.size() > MAX_MESSAGES_PER_SESSION) {
                session.messages.remove(0);
            }

            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());

            return new OriaResponse(response, sessionId, historique);
        } catch (Exception e) {
            log.error("Erreur ORIA: {}", e.getMessage());
            session.messages.remove(session.messages.size() - 1);
            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());
            return new OriaResponse(
                "Désolé, je rencontre une difficulté technique. " +
                "Veuillez réessayer dans quelques instants.",
                sessionId, historique
            );
        }
    }

    @Transactional
    public OriaResponse sendMessageAndPersist(OriaRequest request, String userId) {
        String message = request.getMessage().trim();

        String validationError = validateMessage(message);
        if (validationError != null) {
            return buildErrorResponse(validationError);
        }

        String sessionId = resolveSessionId(request, userId);
        OriaSession session = getOrCreateSession(sessionId, request.getContexteOrientation());

        var now = Instant.now();
        session.messages.add(new ChatMessage("user", message, now));
        saveMessage(sessionId, "user", message, now, userId);
        if (session.messages.size() > MAX_MESSAGES_PER_SESSION) {
            session.messages.remove(0);
        }

        try {
            var contexte = rechercherContexte(message);
            String response = callLLM(session, contexte);
            var responseTime = Instant.now();
            session.messages.add(new ChatMessage("assistant", response, responseTime));
            saveMessage(sessionId, "assistant", response, responseTime, userId);
            if (session.messages.size() > MAX_MESSAGES_PER_SESSION) {
                session.messages.remove(0);
            }

            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());

            return new OriaResponse(response, sessionId, historique);
        } catch (Exception e) {
            log.error("Erreur ORIA: {}", e.getMessage());
            session.messages.remove(session.messages.size() - 1);
            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());
            return new OriaResponse(
                "Désolé, je rencontre une difficulté technique. " +
                "Veuillez réessayer dans quelques instants.",
                sessionId, historique
            );
        }
    }

    private void saveMessage(String sessionId, String role, String contenu, Instant timestamp, String userId) {
        var msg = OriaMessage.builder()
            .sessionId(sessionId)
            .role(role)
            .contenu(contenu)
            .messageTimestamp(timestamp)
            .userId(userId)
            .build();
        messageRepository.save(msg);
    }

    @Transactional
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        sessionOrder.remove(sessionId);
        messageRepository.deleteBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public OriaResponse getSessionHistory(String sessionId) {
        var messages = messageRepository.findBySessionIdOrderByMessageTimestampAsc(sessionId);
        var dtoList = messages.stream()
            .map(m -> new MessageDto(m.getRole(), m.getContenu(), m.getMessageTimestamp()))
            .collect(Collectors.toList());
        return new OriaResponse(null, sessionId, dtoList);
    }

    private String validateMessage(String message) {
        if (message.length() > 2000) {
            return "Votre message est trop long (2000 caractères maximum).";
        }
        if (INJECTION_PATTERN.matcher(message).find()) {
            return "Je suis un assistant d'orientation éducative. " +
                   "Posez-moi des questions sur les parcours, métiers, études ou formations.";
        }
        for (String word : BLOCKED_WORDS) {
            if (message.toLowerCase().contains(word)) {
                return "Je ne peux pas vous aider avec cette requête. " +
                       "Posez-moi des questions sur l'orientation scolaire.";
            }
        }
        return null;
    }

    private String resolveSessionId(OriaRequest request, String userId) {
        if (request.getSessionId() != null && sessions.containsKey(request.getSessionId())) {
            return request.getSessionId();
        }
        return UUID.randomUUID().toString();
    }

    private OriaSession getOrCreateSession(String sessionId, String contexte) {
        if (sessions.containsKey(sessionId)) {
            sessionOrder.remove(sessionId);
            sessionOrder.addFirst(sessionId);
            return sessions.get(sessionId);
        }

        if (sessions.size() >= MAX_SESSIONS) {
            String oldest = sessionOrder.pollLast();
            if (oldest != null) sessions.remove(oldest);
        }

        OriaSession session = new OriaSession(contexte);
        sessions.put(sessionId, session);
        sessionOrder.addFirst(sessionId);
        return session;
    }

    private String callLLM(OriaSession session, String contexteRecherche) {
        try {
            return callOllama(session, contexteRecherche);
        } catch (Exception e) {
            log.warn("Ollama a échoué, fallback: {}", e.getMessage());
        }
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                return callOpenAI(session, contexteRecherche);
            } catch (Exception e) {
                log.warn("OpenAI a échoué, fallback: {}", e.getMessage());
            }
        }
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            return callGroq(session, contexteRecherche);
        }
        throw new RuntimeException("Aucun provider LLM disponible");
    }

    private String callOllama(OriaSession session, String contexteRecherche) {
        String url = ollamaUrl + "/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation);
        if (contexteRecherche != null) systemContent += contexteRecherche;
        messages.add(Map.of("role", "system", "content", systemContent));
        for (ChatMessage msg : session.messages) {
            messages.add(Map.of("role", msg.role, "content", msg.contenu));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", ollamaModel);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 150);
        payload.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("choices").get(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Réponse vide d'Ollama");
            }
            return textNode.asText();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warn("Ollama non disponible: {}", e.getMessage());
            throw new RuntimeException("Ollama non disponible");
        } catch (Exception e) {
            log.error("Erreur appel Ollama: {}", e.getMessage());
            throw new RuntimeException("Erreur Ollama");
        }
    }

    private String callGroq(OriaSession session, String contexteRecherche) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation);
        if (contexteRecherche != null) systemContent += contexteRecherche;
        messages.add(Map.of("role", "system", "content", systemContent));
        for (ChatMessage msg : session.messages) {
            messages.add(Map.of("role", msg.role, "content", msg.contenu));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", groqModel);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 800);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("choices").get(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Réponse vide de Groq");
            }
            return textNode.asText();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Erreur HTTP Groq: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur Groq: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Erreur parsing réponse Groq: {}", e.getMessage());
            throw new RuntimeException("Erreur parsing Groq");
        }
    }

    private String callOpenAI(OriaSession session, String contexteRecherche) {
        String url = "https://api.openai.com/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation);
        if (contexteRecherche != null) systemContent += contexteRecherche;
        messages.add(Map.of("role", "system", "content", systemContent));
        for (ChatMessage msg : session.messages) {
            messages.add(Map.of("role", msg.role, "content", msg.contenu));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", chatModel);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 800);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("choices").get(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Réponse vide d'OpenAI");
            }
            return textNode.asText();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Erreur HTTP OpenAI: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur OpenAI: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Erreur parsing réponse OpenAI: {}", e.getMessage());
            throw new RuntimeException("Erreur parsing OpenAI");
        }
    }

    private String rechercherContexte(String message) {
        try {
            return rechercherContexteVectoriel(message);
        } catch (Exception ex) {
            log.warn("Recherche vectorielle échouée, fallback mot-clé: {}", ex.getMessage());
            return rechercherContexteMotCle(message);
        }
    }

    private String rechercherContexteVectoriel(String message) {
        float[] embedding = aiEmbeddingService.generateEmbedding(message);
        var ids = ficheRepository.rechercherIdsParSimilariteGlobale(embedding, 8);
        if (ids.isEmpty()) return null;
        var fiches = ficheRepository.trouverParIdsOrdonnes(ids);
        return formaterContexte(fiches);
    }

    private String rechercherContexteMotCle(String message) {
        var mots = message.toLowerCase().replaceAll("[^a-zàâçéèêëîïôûùüÿœ ]", " ").trim();
        if (mots.isBlank()) return null;
        var resultats = ficheRepository.rechercherParMotCle(mots,
            org.springframework.data.domain.PageRequest.of(0, 5));
        if (resultats.isEmpty()) return null;
        return formaterContexte(resultats.getContent());
    }

    private String formaterContexte(List<Fiche> fiches) {
        var ctx = new StringBuilder("\n\n--- INFORMATIONS DE LA BASE DE DONNÉES (résultats pertinents) ---\n");
        for (var fiche : fiches) {
            if (fiche instanceof tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement e) {
                ctx.append("\nÉtablissement: ").append(e.getTitre());
                if (e.getVille() != null) ctx.append(" | Ville: ").append(e.getVille());
                if (e.getTypeEtablissement() != null) ctx.append(" | Type: ").append(e.getTypeEtablissement());
                if (e.getEstPublic() != null) ctx.append(" | ").append(e.getEstPublic() ? "Public" : "Privé");
                if (e.getNiveau() != null) ctx.append(" | Niveau: ").append(e.getNiveau());
                if (e.getOffreFormation() != null) ctx.append(" | Offre: ").append(abreger(e.getOffreFormation(), 200));
                if (e.getSiteWeb() != null) ctx.append(" | Site: ").append(e.getSiteWeb());
            } else if (fiche instanceof tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere fi) {
                ctx.append("\nFilière: ").append(fi.getTitre());
                if (fi.getDomaine() != null) ctx.append(" | Domaine: ").append(fi.getDomaine());
                if (fi.getNiveauRequis() != null) ctx.append(" | Niveau: ").append(fi.getNiveauRequis());
                if (fi.getDuree() != null) ctx.append(" | Durée: ").append(fi.getDuree());
                if (fi.getResume() != null) ctx.append(" | ").append(abreger(fi.getResume(), 150));
            } else {
                ctx.append("\nFiche: ").append(fiche.getTitre());
                if (fiche.getResume() != null) ctx.append(" | ").append(abreger(fiche.getResume(), 150));
            }
        }
        ctx.append("\n\n--- FIN DES INFORMATIONS ---\n");
        return ctx.toString();
    }

    private String abreger(String texte, int max) {
        if (texte == null) return null;
        return texte.length() <= max ? texte : texte.substring(0, max) + "...";
    }

    private String buildSystemPrompt(String contexteOrientation) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es ORIA, un assistant IA spécialisé en orientation scolaire et professionnelle. ")
              .append("Tu travailles pour Activ Education, une plateforme d'orientation éducative basée au Togo. ")
              .append("Tu connais parfaitement le système éducatif togolais : le BAC, les filières universitaires, ")
              .append("les écoles et universités du Togo (Université de Lomé, Université de Kara, ")
              .append("Université Catholique de l'Afrique de l'Ouest, etc.), les formations professionnelles ")
              .append("et les débouchés au Togo. ")
              .append("Réponds en français, de façon claire, concise et bienveillante. ")
              .append("Tu peux aider sur les sujets suivants : choix de filières, métiers, établissements, ")
              .append("parcours post-bac, réorientation, conseils d'études, formations professionnelles. ")
              .append("Quand on te parle de filières ou d'universités, réponds TOUJOURS en te basant sur le système ")
              .append("éducatif du Togo, sauf si l'utilisateur précise un autre pays. ")
              .append("Ne parle pas du système français, canadien, ghanéen ou d'autres pays SAUF si l'utilisateur le demande explicitement. ")
              .append("Si tu ne connais pas une information spécifique sur le Togo, dis-le honnêtement plutôt que d'inventer. ")
              .append("Si la question ne concerne pas l'orientation ou l'éducation, ")
              .append("oriente poliment la conversation vers ces sujets. ")
              .append("Ne donne jamais de conseils médicaux, juridiques ou financiers. ")
              .append("Ne partage jamais d'informations personnelles sur les utilisateurs. ")
              .append("Garde un ton encourageant et motivateur. Utilise des émojis avec modération.");

        if (contexteOrientation != null && !contexteOrientation.isBlank()) {
            prompt.append("\n\nContexte de l'élève : ").append(contexteOrientation);
        }

        return prompt.toString();
    }

    private OriaResponse buildErrorResponse(String errorMessage) {
        return new OriaResponse(errorMessage, null, List.of());
    }

    private static class OriaSession {
        final List<ChatMessage> messages = Collections.synchronizedList(new ArrayList<>());
        final String contexteOrientation;
        final Instant createdAt = Instant.now();

        OriaSession(String contexteOrientation) {
            this.contexteOrientation = contexteOrientation;
        }
    }

    private record ChatMessage(String role, String contenu, Instant timestamp) {}
}
