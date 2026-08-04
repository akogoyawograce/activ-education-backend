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

        String validationError = validateMessage(message);
        if (validationError != null) {
            return buildErrorResponse(validationError);
        }

        String sessionId = resolveSessionId(request, userId);
        OriaSession session = getOrCreateSession(sessionId, request.getContexteOrientation());

        var now = Instant.now();
        session.messages.add(new ChatMessage("user", message, now));
        saveMessage(sessionId, "user", message, now, userId);
        updateProfilOrientation(userId, message);
        if (session.messages.size() > MAX_MESSAGES_IN_MEMORY) {
            session.messages.remove(0);
        }

        try {
            var contexte = rechercherContexte(message);
            String resume = profilOrientationRepository.findByUserId(userId)
                .map(ProfilOrientation::getResumeParcours)
                .filter(s -> !s.isBlank())
                .orElse(null);
            String response = callLLM(session, contexte, resume);
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
                String[] parts = profil.getDomainesInteret().split(",\\s*");
                Collections.addAll(existants, parts);
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
        return "conv-" + userId;
    }

    private OriaSession getOrCreateSession(String sessionId, String contexte) {
        return sessions.computeIfAbsent(sessionId, k -> new OriaSession(contexte));
    }

    private String callLLM(OriaSession session, String contexteRecherche) {
        return callLLM(session, contexteRecherche, null);
    }

    private String callLLM(OriaSession session, String contexteRecherche, String resumeParcours) {
        try {
            return callOllama(session, contexteRecherche, resumeParcours);
        } catch (Exception e) {
            log.warn("Ollama a échoué, fallback: {}", e.getMessage());
        }
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                return callOpenAI(session, contexteRecherche, resumeParcours);
            } catch (Exception e) {
                log.warn("OpenAI a échoué, fallback: {}", e.getMessage());
            }
        }
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            return callGroq(session, contexteRecherche, resumeParcours);
        }
        throw new RuntimeException("Aucun provider LLM disponible");
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
        return callGroq(session, contexteRecherche, null);
    }

    private String callGroq(OriaSession session, String contexteRecherche, String resumeParcours) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation, resumeParcours);
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
        return callOpenAI(session, contexteRecherche, null);
    }

    private String callOpenAI(OriaSession session, String contexteRecherche, String resumeParcours) {
        String url = "https://api.openai.com/v1/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();
        var systemContent = buildSystemPrompt(session.contexteOrientation, resumeParcours);
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
              .append("5. N'invente jamais un nom d'établissement, un chiffre ou un fait précis. Si tu n'es pas sûr qu'une \n")
              .append("université propose réellement une filière donnée, dis-le clairement plutôt que d'affirmer une \n")
              .append("information non vérifiée. Distingue explicitement ce que tu sais avec certitude de ce qui est une \n")
              .append("estimation ou une piste à vérifier. \n\n")
              .append("6. Format : réponses courtes et claires par défaut, listes à puces pour énumérer des filières/établissements, \n")
              .append("pas de blocs de texte denses. Une seule langue par réponse — jamais de mélange de caractères ou de mots \n")
              .append("d'une autre langue (ex. chinois, anglais) sauf si l'utilisateur écrit lui-même dans cette langue. \n\n")
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
