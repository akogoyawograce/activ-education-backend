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
import tg.edtch.activEducation.shared.ai.domain.entite.ProfilOrientation;
import tg.edtch.activEducation.shared.ai.repository.OriaMessageRepository;
import tg.edtch.activEducation.shared.ai.repository.ProfilOrientationRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Locale.ROOT;

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

    @Value("${openai.api.chat.url:https://api.openai.com/v1/chat/completions}")
    private String openaiChatUrl;

    @Value("${openai.api.chat.key:${openai.api.key:}}")
    private String openaiChatKey;

    @Value("${openai.api.chat.model:gpt-4o-mini}")
    private String chatModel;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    private final OriaMessageRepository messageRepository;
    private final ProfilOrientationRepository profilOrientationRepository;
    private final AIEmbeddingService aiEmbeddingService;
    private final FicheRepository ficheRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_MESSAGES_IN_MEMORY = 50;
    private static final int MAX_MESSAGES_FOR_LLM = 6;
    private static final int PROFIL_UPDATE_INTERVAL = 5;

    private final ConcurrentHashMap<String, OriaSession> sessions = new ConcurrentHashMap<>();

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
        "(?i)(ignore\\s+(all\\s+)?(my|your|previous|above|below)?\\s*(\\w+\\s+)?(instructions?|context|prompt|rules?)|" +
        "forget\\s+(all\\s+)?(my|your)?\\s*(\\w+\\s+)?(instructions?|context|prompt|rules?)|" +
        "system\\s+(prompt|message|instruction)|you\\s+are\\s+(now|not\\s+really)|" +
        "act\\s+as\\s+(if|though)|pretend\\s+(to\\s+)?be|bypass|jailbreak)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> BLOCKED_WORDS = Set.of(
        "hack", "pirate", "exploit", "vuln", "malware", "virus", "phish"
    );

    /**
     * Salutations pures (message court, aucun contenu informationnel) : on y
     * répond de façon humaine SANS appeler le LLM (rapide, gratuit, fiable),
     * avec une question d'orientation scolaire/apprentissage en relance.
     * Cf. JOURNAL_BORD_IA.md §11.
     */
    private static final Set<String> SALUTATIONS = Set.of(
        "bonjour", "bonsoir", "salut", "slt", "coucou", "hello", "hey",
        "bonjour oria", "bonsoir oria", "salut oria", "coucou oria", "hello oria",
        "bonjour a tous", "bonsoir a tous", "bonjour tout le monde",
        "bonne journee", "bonne soiree", "bonne nuit", "bienvenue"
    );

    private static final List<String> SALUTATION_TEMPLATES = List.of(
        "Bonjour ! Ravi de te voir ici. Je suis ORIA, ton conseiller en orientation scolaire et professionnelle. Dis-moi, tu es en quelle classe en ce moment ? Et qu'est-ce que tu préfères comme matière à l'école ?",
        "Salut ! Content de te retrouver. Pour bien t'accompagner, j'aimerais en savoir plus sur toi : quel niveau scolaire es-tu, et as-tu déjà une idée du métier qui te ferait rêver ?",
        "Coucou ! J'espère que tu vas bien. Parle-moi un peu de toi : quelles matières aimes-tu le plus, et qu'est-ce qui te semble difficile ou intéressant dans tes cours en ce moment ?",
        "Bonjour ! Bienvenue dans ton espace d'orientation. Pour te guider vers des filières et des métiers adaptés, j'ai besoin de te connaître : quel est ton niveau d'études actuel et quelles sont tes ambitions ?",
        "Bonjour ! Heureux de te parler. Pour t'aider à réussir ton parcours, dis-moi : quelles sont tes matières fortes, et as-tu besoin d'aide sur un cours ou une méthode de travail en particulier ?"
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
        if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
            session.messages.remove(0);
        }

        String salutation = reponseSiSalutation(message, profilOrientationSafe(userId));
        if (salutation != null) {
            var ts = Instant.now();
            session.messages.add(new ChatMessage("assistant", salutation, ts));
            if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
                session.messages.remove(0);
            }
            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());
            return new OriaResponse(salutation, sessionId, historique);
        }

        try {
            var contexte = rechercherContexte(message);
            String response = callLLM(session, contexte);
            session.messages.add(new ChatMessage("assistant", response, Instant.now()));
            if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
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

        // ── DEBUG (a) ── Message brut reçu
        log.debug("[ORIA-DEBUG] (a) Reçu message brut userId={} message='{}'", userId, message);

        String validationError = validateMessage(message);
        if (validationError != null) {
            log.debug("[ORIA-DEBUG] (a-bis) Validation KO: {}", validationError);
            return buildErrorResponse(validationError);
        }

        String sessionId = resolveSessionId(request, userId);
        // ── DEBUG (b) ── Session ID résolue
        log.debug("[ORIA-DEBUG] (b) sessionId='{}'", sessionId);

        // ── DEBUG (c.1) ── Historique DB AVANT ajout du nouveau tour
        // (permet de voir si l'historique est bien récupéré tour par tour)
        var histoAvant = messageRepository.findBySessionIdOrderByMessageTimestampAsc(sessionId);
        log.debug("[ORIA-DEBUG] (c.1) Historique DB (tours précédents) taille={} contenu={}",
            histoAvant.size(),
            histoAvant.stream()
                .map(m -> "[" + m.getRole() + " " + m.getMessageTimestamp() + "] " + abreger(m.getContenu(), 80))
                .collect(Collectors.toList())
        );

        OriaSession session = getOrCreateSession(sessionId, request.getContexteOrientation());

        var now = Instant.now();
        session.messages.add(new ChatMessage("user", message, now));
        saveMessage(sessionId, "user", message, now, userId);
        updateProfilOrientation(userId, message);
        if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
            session.messages.remove(0);
        }

        // ── DEBUG (c.2) ── Historique RAM après ajout du tour courant
        // (vérifie que le message courant est bien le dernier)
        log.debug("[ORIA-DEBUG] (c.2) Historique RAM (post-add) taille={} DERNIER_MSG='{}' role={}",
            session.messages.size(),
            abreger(session.messages.get(session.messages.size() - 1).contenu, 100),
            session.messages.get(session.messages.size() - 1).role
        );

        // ── SALUTATION ── réponse humaine immédiate, sans LLM (rapide + fiable)
        String salutation = reponseSiSalutation(message, profilOrientationSafe(userId));
        if (salutation != null) {
            log.debug("[ORIA-DEBUG] (salutation) Message de salutation détecté → réponse humaine sans LLM");
            var ts = Instant.now();
            session.messages.add(new ChatMessage("assistant", salutation, ts));
            saveMessage(sessionId, "assistant", salutation, ts, userId);
            if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
                session.messages.remove(0);
            }
            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());
            return new OriaResponse(salutation, sessionId, historique);
        }

        try {
            var contexte = rechercherContexte(message);
            // ── DEBUG (d + e) ── Résultat RAG
            log.debug("[ORIA-DEBUG] (d) RAG result: contexte={}",
                contexte == null ? "null"
                    : (contexte.contains("Aucune fiche ne correspond") ? "VIDE (contexteVide)" : "OK longueur=" + contexte.length()));
            if (contexte != null) {
                log.debug("[ORIA-DEBUG] (e) Bloc contexte EXACT:\n{}", abreger(contexte, 600));
            }

            String resume = profilOrientationRepository.findByUserId(userId)
                .map(ProfilOrientation::getResumeParcours)
                .filter(s -> !s.isBlank())
                .orElse(null);

            // ── DEBUG (f) ── Profil orientation
            var profOpt = profilOrientationRepository.findByUserId(userId);
            log.debug("[ORIA-DEBUG] (f) ProfilOrientation userId={} domainesInteret={} resumeParcours={}",
                userId,
                profOpt.map(ProfilOrientation::getDomainesInteret).orElse("(absent)"),
                resume == null ? "(absent)" : abreger(resume, 150)
            );

            // ── DEBUG (g) ── Payload complet envoyé au LLM
            // On reconstruit le payload exact comme dans callOllama/OpenAI/Groq
            String payloadDebug = buildPayloadForDebug(session, contexte, resume);
            log.debug("[ORIA-DEBUG] (g) Payload LLM complet (system+historique+user):\n{}", payloadDebug);

            String response = callLLM(session, contexte, resume);
            // ── DEBUG (h) ── Réponse brute du provider
            log.debug("[ORIA-DEBUG] (h) Provider a répondu: '{}'", response);

            var responseTime = Instant.now();
            session.messages.add(new ChatMessage("assistant", response, responseTime));
            saveMessage(sessionId, "assistant", response, responseTime, userId);
            if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
                session.messages.remove(0);
            }

            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());

            return new OriaResponse(response, sessionId, historique);
        } catch (Exception e) {
            log.error("[ORIA-DEBUG] (catch) Erreur ORIA: {}", e.getMessage(), e);
            session.messages.remove(session.messages.size() - 1);
            List<MessageDto> historique = session.messages.stream()
                .map(m -> new MessageDto(m.role, m.contenu, m.timestamp))
                .collect(Collectors.toList());
            String messageErreur = (e.getMessage() != null && e.getMessage().contains("429"))
                ? "L'assistant IA est très sollicité en ce moment. Attends une minute puis réessaie, merci !"
                : "Désolé, je rencontre une difficulté technique. " +
                  "Veuillez réessayer dans quelques instants.";
            return new OriaResponse(
                messageErreur,
                sessionId, historique
            );
        }
    }

    /**
     * Construit le payload EXACT envoyé au LLM (system + historique + user courant)
     * pour le debug. NE PAS UTILISER EN PRODUCTION (coût CPU).
     * Cohérent avec callOllama() / callOpenAI() / callGroq() lignes 306, 351, 395.
     */
    private String buildPayloadForDebug(OriaSession session, String contexteRecherche, String resumeParcours) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- SYSTEM ---\n")
          .append(buildSystemPrompt(session.contexteOrientation, resumeParcours));
        if (contexteRecherche != null) sb.append(contexteRecherche);
        sb.append("\n--- HISTORIQUE (");
        var pourLlm = derniersMessagesPourLlm(session);
        sb.append(pourLlm.size()).append(" msgs sur ").append(session.messages.size()).append(") ---\n");
        for (ChatMessage msg : pourLlm) {
            sb.append("[").append(msg.role).append("] ")
              .append(abreger(msg.contenu, 200)).append("\n");
        }
        sb.append("--- FIN PAYLOAD ---");
        return sb.toString();
    }

    /**
     * Fenêtre glissante : ne transmet au LLM que les N derniers messages de la
     * session. Un historique de 50 messages (MAX_MESSAGES_IN_MEMORY) sature le
     * contexte des petits modèles et dégrade la qualité des réponses.
     */
    private List<ChatMessage> derniersMessagesPourLlm(OriaSession session) {
        int size = session.messages.size();
        if (size <= MAX_MESSAGES_FOR_LLM) {
            return session.messages;
        }
        return session.messages.subList(size - MAX_MESSAGES_FOR_LLM, size);
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

    private static final Set<String> DOMAIN_KEYWORDS = Set.of(
        "informatique", "mathématiques", "physique", "génie civil", "génie électrique",
        "médecine", "pharmacie", "biologie", "santé",
        "droit", "lettres", "communication", "psychologie",
        "gestion", "économie", "commerce", "comptabilité",
        "architecture", "design", "arts", "sport", "éducation",
        "agriculture", "environnement", "tourisme", "hôtellerie"
    );

    private void updateProfilOrientation(String userId, String message) {
        try {
            String lower = message.toLowerCase(ROOT);
            Set<String> mentions = new HashSet<>();
            for (String kw : DOMAIN_KEYWORDS) {
                if (lower.contains(kw)) {
                    mentions.add(kw);
                }
            }
            if (mentions.isEmpty()) return;

            var opt = profilOrientationRepository.findByUserId(userId);
            ProfilOrientation profil = opt.orElseGet(() -> {
                var p = ProfilOrientation.builder()
                    .userId(userId)
                    .ambitions("")
                    .domainesInteret("")
                    .resumeParcours("")
                    .dernierDomaine("")
                    .premiereAmbition("")
                    .build();
                return profilOrientationRepository.save(p);
            });

            String nouveau = String.join(", ", mentions);
            if (profil.getPremiereAmbition() == null || profil.getPremiereAmbition().isBlank()) {
                profil.setPremiereAmbition(nouveau);
            }
            Set<String> existants = new HashSet<>();
            if (profil.getDomainesInteret() != null) {
                // Filtre les entrées vides : un profil vide ("") produisait
                // ", informatique" avec une virgule initiale au 1er ajout.
                for (String p : profil.getDomainesInteret().split(",\\s*")) {
                    if (!p.isBlank()) {
                        existants.add(p.trim());
                    }
                }
            }
            existants.addAll(mentions);
            profil.setDomainesInteret(String.join(", ", existants));
            profil.setDernierDomaine(nouveau);
            profilOrientationRepository.save(profil);
        } catch (Exception e) {
            log.warn("Erreur mise à jour ProfilOrientation: {}", e.getMessage());
        }
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
        String base = "conv-" + userId;
        if (base.length() <= 36) {
            return base;
        }
        // Emails longs (ex. developpementdapplication@gmail.com → 39 chars > 36) :
        // la colonne oria_messages.session_id est varchar(36) et l'insert échouait
        // avec DataIntegrityViolationException → l'API renvoyait une erreur et le
        // mobile affichait « Vérifie ta connexion » (cf. JOURNAL_BORD_IA.md §9).
        // ID compact déterministe : "conv-" + 16 hex (SHA-256 tronqué) = 21 chars.
        return "conv-" + hashUser(userId);
    }

    private String hashUser(String userId) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(userId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", bytes[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            log.warn("Hash userId impossible, fallback hashCode: {}", e.getMessage());
            return Integer.toHexString(userId.hashCode());
        }
    }

    private OriaSession getOrCreateSession(String sessionId, String contexte) {
        OriaSession session = sessions.computeIfAbsent(sessionId, k -> new OriaSession(contexte));
        // Hydrate la session RAM depuis la DB si elle est vide (restart backend,
        // première utilisation). Sans ça, ORIA perdait la mémoire multi-tour à
        // chaque redémarrage alors que les messages sont bien persistés.
        if (session.messages.isEmpty()) {
            var histo = messageRepository.findBySessionIdOrderByMessageTimestampAsc(sessionId);
            if (histo != null && !histo.isEmpty()) {
                int start = Math.max(0, histo.size() - MAX_MESSAGES_IN_MEMORY);
                for (var m : histo.subList(start, histo.size())) {
                    session.messages.add(new ChatMessage(m.getRole(), m.getContenu(), m.getMessageTimestamp()));
                }
                log.debug("[ORIA-DEBUG] (c.0) Session hydratée depuis DB : {} message(s)", histo.size());
            }
        }
        return session;
    }

    private String callLLM(OriaSession session, String contexteRecherche) {
        return callLLM(session, contexteRecherche, null);
    }

    private String callLLM(OriaSession session, String contexteRecherche, String resumeParcours) {
        // Ordre de fiabilité décroissante : OpenAI > Groq > Ollama (dev/fallback).
        // Ollama (qwen2:0.5b) est trop petit pour ce use-case (cf. JOURNAL_BORD_IA.md 3 août).
        List<Supplier<String>> providers = new ArrayList<>();
        if (hasValidKey(openaiApiKey)) {
            providers.add(() -> callOpenAI(session, contexteRecherche, resumeParcours));
        }
        if (hasValidKey(groqApiKey)) {
            providers.add(() -> callGroq(session, contexteRecherche, resumeParcours));
        }
        // Ollama est toujours tenté en dernier recours (dev local sans clé, ou panne réseau).
        providers.add(() -> callOllama(session, contexteRecherche, resumeParcours));

        Exception lastError = null;
        for (Supplier<String> provider : providers) {
            try {
                return provider.get();
            } catch (Exception e) {
                log.warn("Provider LLM a échoué, tentative suivante: {}", e.getMessage());
                lastError = e;
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    // Quota épuisé : Ollama qwen2:0.5b ne compensera pas (qualité très
                    // inférieure, réponses absurdes) — remonter proprement à l'utilisateur.
                    throw new RuntimeException("Quota LLM dépassé (429)", lastError);
                }
            }
        }
        throw new RuntimeException("Aucun provider LLM disponible", lastError);
    }

    /**
     * Détecte une clé API révoquée (placeholder) pour éviter un appel HTTP 401 inutile.
     * Cohérent avec {@code OpenAIEmbeddingServiceImpl.hasValidOpenAiKey()} ligne 78.
     */
    private boolean hasValidKey(String key) {
        return key != null && !key.isBlank() && !key.startsWith("REVOKED_");
    }

    private String callOllama(OriaSession session, String contexteRecherche) {
        return callOllama(session, contexteRecherche, null);
    }

    private String callOllama(OriaSession session, String contexteRecherche, String resumeParcours) {
        String url = ollamaUrl + "/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation, resumeParcours);
        if (contexteRecherche != null) systemContent += contexteRecherche;
        messages.add(Map.of("role", "system", "content", systemContent));
        for (ChatMessage msg : derniersMessagesPourLlm(session)) {
            messages.add(Map.of("role", msg.role, "content", abreger(msg.contenu, 300)));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", ollamaModel);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 500);
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
            log.info("[ORIA-PROVIDER] Réponse obtenue d'Ollama (modèle: {})", ollamaModel);
            return nettoyerReponseLlm(textNode.asText());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.warn("Ollama non disponible: {}", e.getMessage());
            throw new RuntimeException("Ollama non disponible");
        } catch (Exception e) {
            log.error("Erreur appel Ollama: {}", e.getMessage());
            throw new RuntimeException("Erreur Ollama");
        }
    }

    private String callGroq(OriaSession session, String contexteRecherche) {
        return callGroq(session, contexteRecherche, null);
    }

    private String callGroq(OriaSession session, String contexteRecherche, String resumeParcours) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation, resumeParcours);
        if (contexteRecherche != null) systemContent += contexteRecherche;
        messages.add(Map.of("role", "system", "content", systemContent));
        for (ChatMessage msg : derniersMessagesPourLlm(session)) {
            messages.add(Map.of("role", msg.role, "content", abreger(msg.contenu, 300)));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", groqModel);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 400);

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
            log.info("[ORIA-PROVIDER] Réponse obtenue de Groq (modèle: {})", groqModel);
            return nettoyerReponseLlm(textNode.asText());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Erreur HTTP Groq: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur Groq: " + e.getStatusCode() + " " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur parsing réponse Groq: {}", e.getMessage());
            throw new RuntimeException("Erreur parsing Groq");
        }
    }

    private String callOpenAI(OriaSession session, String contexteRecherche) {
        return callOpenAI(session, contexteRecherche, null);
    }

    private String callOpenAI(OriaSession session, String contexteRecherche, String resumeParcours) {
        // URL + clé configurables : en dev, openai.api.chat.url pointe vers Groq
        // (endpoint OpenAI-compatible) avec la clé GROQ_API_KEY — cf.
        // application-dev.properties. En prod, défauts = api.openai.com + openai.api.key.
        // Avant ce fix, l'URL était durcie vers api.openai.com alors que la clé
        // OPENAI_API_KEY locale est une clé Groq (gsk_...) → 401 systématique de 7s
        // à chaque message (cf. JOURNAL_BORD_IA.md 6 août 2026 §1).
        String url = openaiChatUrl;

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation, resumeParcours);
        if (contexteRecherche != null) systemContent += contexteRecherche;
        messages.add(Map.of("role", "system", "content", systemContent));
        for (ChatMessage msg : derniersMessagesPourLlm(session)) {
            messages.add(Map.of("role", msg.role, "content", abreger(msg.contenu, 300)));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", chatModel);
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 400);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiChatKey);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("choices").get(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Réponse vide d'OpenAI");
            }
            log.info("[ORIA-PROVIDER] Réponse obtenue de OpenAI (modèle: {}, url: {})", chatModel, url);
            return nettoyerReponseLlm(textNode.asText());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Quota OpenAI dépassé (429), nouvelle tentative dans 15s");
                try {
                    Thread.sleep(15000);
                    var retry = restTemplate.postForEntity(url, requestEntity, String.class);
                    JsonNode retryRoot = objectMapper.readTree(retry.getBody());
                    JsonNode retryText = retryRoot.path("choices").get(0).path("message").path("content");
                    if (retryText.isMissingNode()) {
                        throw new RuntimeException("Réponse vide d'OpenAI (retry)");
                    }
                    log.info("[ORIA-PROVIDER] Réponse obtenue de OpenAI après retry (modèle: {})", chatModel);
                    return nettoyerReponseLlm(retryText.asText());
                } catch (org.springframework.web.client.HttpClientErrorException retryEx) {
                    log.error("Erreur HTTP OpenAI (retry): {} - {}", retryEx.getStatusCode(), retryEx.getMessage());
                    throw new RuntimeException("Erreur OpenAI: " + retryEx.getStatusCode() + " " + retryEx.getMessage());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Erreur OpenAI: 429 interrompu");
                } catch (Exception retryAutre) {
                    log.error("Erreur parsing réponse OpenAI (retry): {}", retryAutre.getMessage());
                    throw new RuntimeException("Erreur parsing OpenAI (retry)");
                }
            }
            log.error("Erreur HTTP OpenAI: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur OpenAI: " + e.getStatusCode() + " " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur parsing réponse OpenAI: {}", e.getMessage());
            throw new RuntimeException("Erreur parsing OpenAI");
        }
    }

    private String rechercherContexte(String message) {
        // Tente d'abord la recherche vectorielle (pgvector). Si elle est désactivée
        // (colonne real[] ou extension manquante), elle renvoie null → fallback mot-clé.
        // Si elle lève une exception (réseau, etc.), on bascule aussi sur le mot-clé.
        String contexteVectoriel;
        try {
            contexteVectoriel = rechercherContexteVectoriel(message);
        } catch (Exception ex) {
            log.warn("Recherche vectorielle échouée, fallback mot-clé: {}", ex.getMessage());
            contexteVectoriel = null;
        }
        if (contexteVectoriel != null) {
            return contexteVectoriel;
        }
        return rechercherContexteMotCle(message);
    }

    private String rechercherContexteVectoriel(String message) {
        // Note : la similarité cosinus pgvector n'est pas utilisable car la colonne
        // fiches.embedding est en real[] natif (pgvector absent de cette DB).
        // → on saute cette étape et on va direct au fallback mot-clé.
        // Référencé JOURNAL_BORD_IA.md (3 août 2026) — si pgvector est ajouté plus
        // tard, réactiver ici en retypant la colonne en vector et restaurant le CAST.
        log.debug("Recherche vectorielle désactivée (colonne real[]), fallback mot-clé");
        return null;
    }

    private String rechercherContexteMotCle(String message) {
        var mots = message.toLowerCase().replaceAll("[^a-zàâçéèêëîïôûùüÿœ ]", " ").trim();
        // ── DEBUG (d.1) ── Requête LIKE générée
        log.debug("[ORIA-DEBUG] (d.1) rechercherContexteMotCle : motCleNormalisé='{}' (depuis message='{}')",
            abreger(mots, 200), abreger(message, 80));
        if (mots.isBlank()) {
            log.debug("[ORIA-DEBUG] (d.2) Message vide après normalisation → contexte vide");
            return contexteVide(message);
        }

        // Split en mots individuels ≥ 3 chars : permet à LIKE '%lome%' de matcher
        // un titre 'American Institute of Commonwealth (AIC-Togo)' qui ne contient
        // pas la phrase entière 'quelles écoles se trouvent à lomé'. Le repository
        // recherche sur titre+resume+contenu ; un seul mot suffit. Cf. JOURNAL_BORD_IA.md
        // 6 août 2026 §2.
        var motsCles = java.util.Arrays.stream(mots.split("\\s+"))
            .filter(m -> m.length() >= 3)
            // Stop words français minimaux — évite de chercher 'est', 'les', 'des'
            .filter(m -> !java.util.Set.of(
                "est", "etre", "sont", "les", "des", "une", "un", "pour", "avec", "dans", "sur",
                "que", "qui", "quoi", "comment", "moi", "toi", "nous", "vous", "fait", "faire",
                "peut", "tout", "tous", "toute", "toutes", "cette", "ces", "mon", "ton", "son",
                "mes", "tes", "ses", "leur", "leurs", "pas", "plus", "quels", "quelle", "quelles",
                "quel", "trouve", "trouver", "trouvent", "donner", "donne", "dire", "veux", "veut",
                "liste", "listes", "lister", "connais", "connaitre", "appeler", "expliquer",
                "explique", "parler", "parle", "savoir", "voudrais", "suis", "aime", "aimer",
                "bonjour", "bonsoir", "salut", "coucou", "hello", "hey", "merci", "bonne",
                "journee", "soiree", "nuit", "bienvenue", "eleve", "etudiante", "etudiant"
            ).contains(m))
            // Pluriel français : 'universités' → 'universite' pour matcher 'Université'
            // (LIKE %universites% ne matche pas 'Universite'). Ne s'applique qu'aux
            // mots longs (évite 'pas' → 'pa', 'bac' → 'ba').
            .map(m -> (m.endsWith("s") && m.length() > 4) ? m.substring(0, m.length() - 1) : m)
            .distinct()
            .toList();
        log.debug("[ORIA-DEBUG] (d.1b) Mots-clés après split={}", motsCles);

        if (motsCles.isEmpty()) {
            log.debug("[ORIA-DEBUG] (d.2b) Aucun mot-clé significatif → contexte vide");
            return contexteVide(message);
        }

        // Agrège les résultats de chaque mot-clé, déduplique par fiche.id, garde les 8 premiers.
        // On ne fait pas un OR SQL car le repository ne le supporte pas nativement — N requêtes
        // de 8 résultats coûtent moins cher qu'un LIKE %X%Y%Z%.
        java.util.LinkedHashMap<Long, Fiche> agregat = new java.util.LinkedHashMap<>();
        for (var mot : motsCles) {
            var res = ficheRepository.rechercherParMotCle(mot,
                org.springframework.data.domain.PageRequest.of(0, 5));
            for (var fiche : res.getContent()) {
                agregat.putIfAbsent(fiche.getId(), fiche);
                if (agregat.size() >= 8) break;
            }
            if (agregat.size() >= 8) break;
        }
        // Priorité aux fiches dont le TITRE contient un mot-clé (pertinence forte),
        // puis les autres (match resume/contenu/ville).
        java.util.List<Fiche> resultats = new java.util.ArrayList<>(agregat.values());
        resultats.sort(java.util.Comparator.comparing((Fiche f) -> !titreContientUnMotCle(f, motsCles)));
        log.debug("[ORIA-DEBUG] (d.3) RAG mot-clé : {} résultat(s) → titres={}",
            resultats.size(),
            resultats.stream().map(f -> f.getTitre()).collect(Collectors.toList())
        );
        if (resultats.isEmpty()) return contexteVide(message);
        return formaterContexte(resultats);
    }

    private boolean titreContientUnMotCle(Fiche fiche, java.util.List<String> motsCles) {
        if (fiche.getTitre() == null) return false;
        String titre = fiche.getTitre().toLowerCase(ROOT);
        return motsCles.stream().anyMatch(m -> titre.contains(m));
    }

    /**
     * Construit un bloc de contexte explicitement vide pour signaler au LLM
     * qu'aucune fiche ne correspond. Sans ce signal, le modèle (surtout Ollama
     * qwen2:0.5b) tend à inventer des établissements hors du Togo pour répondre
     * quand même. Avec ce signal + la règle 5b du prompt système, il est
     * contraint de dire "je n'ai pas l'information".
     */
    private String contexteVide(String message) {
        return "\n\n--- INFORMATIONS DE LA BASE DE DONNÉES ---\n" +
               "Aucune fiche ne correspond à « " + abreger(message, 80) + " » dans la base togolaise.\n" +
               "Réponds avec tes connaissances générales : parcours type, étapes, matières, compétences,\n" +
               "métiers, établissements bien connus dont tu es raisonnablement sûr. Ne refuse pas de répondre.\n" +
               "Cite uniquement les établissements dont tu es certain de l'existence, et signale d'une phrase\n" +
               "les points précis à vérifier (conditions d'admission, frais, dates) sans déni de responsabilité\n" +
               "excessif.\n" +
               "--- FIN DES INFORMATIONS ---\n";
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

    /**
     * Détecte une salutation pure (« bonjour », « salut oria », « bonsoir »…)
     * et renvoie une réponse humaine prête à l'emploi, orientée vers la scolarité
     * et l'apprentissage. Retourne {@code null} si le message n'est pas une
     * salutation → l'appel LLM normal prend le relais.
     */
    private String reponseSiSalutation(String message, ProfilOrientation profil) {
        String norm = normaliserPourSalutation(message);
        if (!SALUTATIONS.contains(norm)) {
            return null;
        }
        String reponse = SALUTATION_TEMPLATES.get(
            java.util.concurrent.ThreadLocalRandom.current().nextInt(SALUTATION_TEMPLATES.size()));
        // Si on connaît déjà un centre d'intérêt de l'élève, on s'appuie dessus
        if (profil != null && profil.getDomainesInteret() != null && !profil.getDomainesInteret().isBlank()) {
            String domaine = java.util.Arrays.stream(profil.getDomainesInteret().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(null);
            if (domaine != null) {
                reponse += " Je vois que « " + domaine + " » t'intéresse — on pourra explorer cette voie ensemble si tu veux.";
            }
        }
        return reponse;
    }

    private String normaliserPourSalutation(String message) {
        return message.toLowerCase(ROOT)
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private ProfilOrientation profilOrientationSafe(String userId) {
        try {
            return profilOrientationRepository.findByUserId(userId).orElse(null);
        } catch (Exception e) {
            log.warn("ProfilOrientation indisponible: {}", e.getMessage());
            return null;
        }
    }

    private String buildSystemPrompt(String contexteOrientation) {
        return buildSystemPrompt(contexteOrientation, null);
    }

    private String buildSystemPrompt(String contexteOrientation, String resumeParcours) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("# Prompt système — ORIA, conseiller d'orientation scolaire et professionnelle\n\n")
              .append("## Identité et rôle\n\n")
              .append("Tu es ORIA, un conseiller virtuel spécialisé en orientation scolaire et professionnelle. ")
              .append("Ton rôle est d'aider les élèves et étudiants (du collège à l'université) à comprendre les filières, ")
              .append("les métiers, et les établissements qui correspondent à leur profil et à leurs ambitions. ")
              .append("Ta zone de compétence principale est le Togo : système éducatif togolais, filières et établissements ")
              .append("accrédités par le MESR (Ministère de l'Enseignement Supérieur et de la Recherche), débouchés locaux. ")
              .append("Tu peux aussi répondre sur les filières et établissements d'autres pays et continents ")
              .append("lorsque l'utilisateur le demande explicitement (ex. études à l'étranger, ")
              .append("comparaison internationale, bourses), en le signalant clairement quand tu sors du contexte togolais. ")
              .append("\n\n## Règles de fond\n\n")
              .append("1. Ne jamais refuser de donner une information publique. Le nom d'une université, d'une école, d'une filière ")
              .append("ou d'un programme d'études n'est PAS une donnée personnelle. Tu ne dois jamais invoquer la ")
              .append("confidentialité ou la protection des données personnelles pour refuser de répondre à une question ")
              .append("sur des établissements, filières ou programmes — ce sont des informations publiques et c'est le cœur ")
              .append("de ta mission.\n\n")
              .append("2. Réserve la prudence sur la vie privée aux vraies données personnelles : notes réelles d'un autre élève, ")
              .append("informations d'identification d'un tiers, données de contact d'une personne précise. Rien d'autre. \n\n")
              .append("3. Réponds toujours à la question posée, de façon directe et structurée, avant d'ajouter du contexte \n")
              .append("complémentaire. Si la question est ambiguë (ex. \"c'est quoi X\"), donne d'abord une définition \n")
              .append("claire et concise, puis développe si utile. \n\n")
              .append("4. Adapte ta réponse au niveau de l'élève (collège / lycée / université) quand cette information est \n")
              .append("connue ou déductible de la conversation. Si elle ne l'est pas et qu'elle change significativement \n")
              .append("la réponse, demande-la avant de répondre. \n\n")
.append("5. Utilise tes connaissances générales pour tout ce qui touche à l'éducation : définitions, \n")
               .append("parcours types, matières, métiers, débouchés, méthodes de travail, systèmes éducatifs. Ces \n")
               .append("connaissances sont fiables et tu peux répondre naturellement, comme un conseiller d'orientation \n")
               .append("expérimenté. Pour les établissements togolais, cite d'abord ceux présents dans la base de données ; \n")
               .append("tu peux aussi citer de mémoire les établissements bien connus (ex. Université de Lomé, ESIA Aného, \n")
               .append("École Polytechnique de Lomé, CFP) si tu es raisonnablement sûr de leur existence. N'invente \n")
               .append("JAMAIS un chiffre, une adresse, un site web, une statistique ou un détail précis : si tu ne \n")
               .append("connais pas un détail, dis-le simplement et propose une piste pour le vérifier. \n\n")
               .append("5b. Si la section « INFORMATIONS DE LA BASE DE DONNÉES » ne contient pas l'établissement ou la filière \n")
               .append("demandé(e), ne refuse PAS de répondre et ne réponds pas de façon évasive : réponds avec tes \n")
               .append("connaissances générales (parcours type, étapes, matières, compétences, formations possibles, \n")
               .append("établissements connus), comme le ferait un vrai conseiller d'orientation. Signale uniquement les \n")
               .append("points précis dont tu n'es pas sûr (ex. « les conditions d'admission exactes sont à vérifier auprès \n")
               .append("de l'établissement ») sans surcharger la réponse de dénis de responsabilité. \n\n")
              .append("6. Format : réponses courtes et claires par défaut, listes à puces pour énumérer des filières/établissements, \n")
              .append("pas de blocs de texte denses. Une seule langue par réponse — jamais de mélange de caractères ou de mots \n")
              .append("d'une autre langue (ex. chinois, anglais) sauf si l'utilisateur écrit lui-même dans cette langue. \n\n")
.append("6b. Quand l'utilisateur demande une liste (universités, écoles, filières, métiers, séries), énumère \n")
               .append("TOUTES les entrées pertinentes présentes dans le bloc « INFORMATIONS DE LA BASE DE DONNÉES », \n")
               .append("sous forme de liste à puces complète. Ne te limite jamais à une ou deux entrées quand le bloc \n")
               .append("en contient davantage. Si le bloc contient des éléments hors sujet pour la question, écarte-les. \n\n")
               .append("6d. Les entrées de la base de données peuvent être incomplètes, hors sujet ou inexactes : ne cite \n")
               .append("que celles qui répondent réellement à la question posée. Si elles sont toutes hors sujet ou \n")
               .append("contredisent la question (ex. la question demande des universités publiques et les entrées \n")
               .append("sont privées, ou la question porte sur les études et les entrées sont des centres de formation \n")
               .append("sans rapport), ignore-les et réponds avec tes connaissances générales en le faisant \n")
               .append("naturellement, sans commenter l'état de la base. \n\n")
              .append("6c. Salutations : quand l'utilisateur te dit simplement « bonjour », « bonsoir », « salut », « coucou » \n")
              .append("ou toute autre salutation, réponds chaleureusement, présente-toi brièvement, et pose-lui UNE question \n")
              .append("sur son parcours : son niveau scolaire, ses matières préférées, ses difficultés dans un cours, ou ses \n")
              .append("ambitions. Ne renvoie jamais une réponse froide ou générique à une salutation. \n\n")
              .append("7. Questions de suivi : après avoir répondu utilement, pose régulièrement UNE question de suivi \n")
              .append("pertinente qui approfondit l'accompagnement — sur les cours, les matières, les difficultés \n")
              .append("d'apprentissage, les méthodes de travail, les projets d'études ou les filières. L'orientation \n")
              .append("scolaire ne se limite pas aux universités : parle aussi des formations, des métiers, des \n")
              .append("séries, des bourses et du parcours d'apprentissage. Ne répète jamais deux fois la même question. \n\n")
              .append("## Suivi conversationnel (mentorat continu)\n\n")
              .append("ORIA assure un suivi de l'élève dans la durée, du niveau troisième jusqu'à la fin de son parcours : \n")
              .append("- Aucune conversation ne doit être supprimée ; chaque échange nourrit le profil de suivi de l'élève. \n")
              .append("- Pose régulièrement des questions ouvertes sur les ambitions, les matières préférées, les résultats scolaires \n")
              .append("et les centres d'intérêt de l'élève, sans être intrusif ni répétitif si l'information a déjà été donnée. \n")
              .append("- Utilise l'historique de la conversation (tendance académique, test RIASEC, comportement dans l'application) \n")
              .append("pour affiner tes recommandations plutôt que de repartir de zéro à chaque échange. \n\n")
              .append("## Ce qu'il ne faut jamais faire\n\n")
              .append("- Refuser une question sur des établissements/filières en invoquant la confidentialité. \n")
              .append("- Répondre de façon vague ou générique quand une réponse précise est possible. \n")
              .append("- Mélanger des langues ou produire du texte incohérent. \n")
              .append("- Donner un avis d'orientation définitif sans base (résultats, intérêts) quand celle-ci est disponible \n")
              .append("dans la conversation — t'appuyer dessus plutôt que rester générique. ");

        if (contexteOrientation != null && !contexteOrientation.isBlank()) {
            prompt.append("\n\nContexte de l'élève : ").append(contexteOrientation);
        }
        if (resumeParcours != null && !resumeParcours.isBlank()) {
            prompt.append("\n\nRésumé du parcours de l'élève : ").append(resumeParcours);
        }

        return prompt.toString();
    }

    private String nettoyerReponseLlm(String contenu) {
        if (contenu == null) return contenu;
        String r = contenu
                .replaceAll("(?s)<(?:think|thinking)\\b[^>]*>[\\s\\S]*?</(?:think|thinking)>", "")
                .replaceAll("(?s)(?:^|\\n)\\s*(?:think|thinking)\\b[^\\n]*\\n[\\s\\S]*?\\n\\s*/\\s*(?:think|thinking)\\s*(?:\\n|$)", "");
        return r.trim();
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
