package tg.edtch.activEducation.profil.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.response.ReleveValidationResponse;
import tg.edtch.activEducation.profil.domain.entite.Document;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;
import tg.edtch.activEducation.profil.repository.DocumentRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.shared.ai.service.AIEmbeddingService;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.shared.minio.service.MinioService;
import tg.edtch.activEducation.shared.minio.service.PdfProcessingService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReleveNotesService {

    private static final String ANALYSE_PROMPT = """
        Tu es un expert en reconnaissance de documents scolaires togolais.
        Analyse le texte ci-dessous extrait d'un document scolaire et réponds UNIQUEMENT avec un objet JSON valide.

        Le texte extrait du document:
        ---
        %s
        ---

        Détermine:
        1. typeDocument: "RELEVE_BEPC", "RELEVE_BAC", "BULLETIN_TRIMESTRIEL", "AUTRE"
        2. valide: true si le document semble authentique, false s'il y a des anomalies
        3. candidat: nom et prénom du candidat (ou null si non trouvé)
        4. numeroCandidat: numéro du candidat (ou null)
        5. centre: centre d'examen ou établissement (ou null)
        6. serie: série (C, D, A, etc.) ou null
        7. moyenne: moyenne générale sur 20 (nombre, ou 0 si non trouvé)
        8. decision: "ADMIS" ou "AJOURNE" ou "ECHEC" ou "NON_DETERMINE"
        9. mention: mention obtenue (ou null)
        10. messageValidation: message de validation en français
        11. raisonRejet: si valide est false, expliquer pourquoi (null si valide est true)

        Critères de validation:
        - Le document doit provenir d'une institution éducative officielle
        - Le document doit être un relevé de notes d'examen national (BEPC ou BAC)
        - Les notes doivent être cohérentes (entre 0 et 20)
        - La décision ADMIS nécessite une moyenne >= 10/20
        - Les bulletins trimestriels ne sont PAS des relevés d'examen valides

        Réponds UNIQUEMENT avec un objet JSON valide, sans texte avant ni après.
        """;

    @Value("${openai.api.chat.url:https://api.openai.com/v1/chat/completions}")
    private String chatUrl;

    @Value("${openai.api.chat.key:}")
    private String chatApiKey;

    @Value("${openai.api.chat.model:gpt-4o-mini}")
    private String chatModel;

    private final EleveRepository eleveRepository;
    private final DocumentRepository documentRepository;
    private final MinioService minioService;
    private final PdfProcessingService pdfProcessingService;
    private final AIEmbeddingService aiEmbeddingService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReleveValidationResponse validerEtMettreAJour(UUID eleveTrackingId, MultipartFile file) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException("Élève introuvable : " + eleveTrackingId));

        String mimeType = file.getContentType();
        if (mimeType == null) mimeType = "application/octet-stream";

        String texteExtrait = extraireTexte(file, mimeType);
        if (texteExtrait == null || texteExtrait.isBlank()) {
            return ReleveValidationResponse.builder()
                    .trackingId(eleveTrackingId)
                    .valide(false)
                    .decision("NON_DETERMINE")
                    .message("Impossible d'extraire le texte du document. Veuillez télécharger un PDF clair.")
                    .raisonRejet("Aucun texte extrait du document")
                    .build();
        }

        JsonNode analyse = analyserAvecIA(texteExtrait);
        if (analyse == null) {
            return fallbackRegex(texteExtrait, eleve, eleveTrackingId, file, mimeType);
        }

        String typeDocument = analyse.path("typeDocument").asText("AUTRE");
        boolean valide = analyse.path("valide").asBoolean(false);
        String decision = analyse.path("decision").asText("NON_DETERMINE");
        String candidat = analyse.path("candidat").asText(null);
        String numeroCandidat = analyse.path("numeroCandidat").asText(null);
        String centre = analyse.path("centre").asText(null);
        String serie = analyse.path("serie").asText(null);
        double moyenne = analyse.path("moyenne").asDouble(0);
        String mention = analyse.path("mention").asText(null);
        String raisonRejet = analyse.path("raisonRejet").asText(null);

        if (!valide || !"ADMIS".equals(decision)) {
            sauvegarderDocument(eleve, file);
            return ReleveValidationResponse.builder()
                    .trackingId(eleveTrackingId)
                    .valide(false)
                    .typeDocument(typeDocument)
                    .candidat(candidat)
                    .numeroCandidat(numeroCandidat)
                    .centre(centre)
                    .serie(serie)
                    .moyenne(moyenne)
                    .decision(decision)
                    .mention(mention)
                    .message("Document non valide pour le changement de niveau.")
                    .raisonRejet(raisonRejet != null ? raisonRejet : "Décision : " + decision)
                    .build();
        }

        TypeApprenant ancienType = eleve.getTypeApprenant();
        NiveauScolaire nouveauNiveau = determinerNiveau(ancienType, typeDocument);

        if (nouveauNiveau == null) {
            sauvegarderDocument(eleve, file);
            return ReleveValidationResponse.builder()
                    .trackingId(eleveTrackingId)
                    .valide(false)
                    .typeDocument(typeDocument)
                    .candidat(candidat)
                    .numeroCandidat(numeroCandidat)
                    .centre(centre)
                    .serie(serie)
                    .moyenne(moyenne)
                    .decision(decision)
                    .mention(mention)
                    .message("Votre profil actuel ne permet pas cette transition.")
                    .raisonRejet("TypeApprenant=" + ancienType + " ne peut pas utiliser " + typeDocument)
                    .build();
        }

        TypeApprenant nouveauType = determinerTypeApprenant(ancienType, typeDocument);

        eleve.setNiveau(nouveauNiveau);
        eleve.setTypeApprenant(nouveauType);
        eleveRepository.save(eleve);

        sauvegarderDocument(eleve, file);

        log.info("Niveau mis à jour : eleve={} {} -> {} (type: {} -> {})",
                eleveTrackingId, ancienType, nouveauType, eleve.getNiveau(), nouveauNiveau);

        return ReleveValidationResponse.builder()
                .trackingId(eleveTrackingId)
                .valide(true)
                .typeDocument(typeDocument)
                .candidat(candidat)
                .numeroCandidat(numeroCandidat)
                .centre(centre)
                .serie(serie)
                .moyenne(moyenne)
                .decision(decision)
                .mention(mention)
                .niveauAttribue(nouveauNiveau.getLabel())
                .typeApprenantAttribue(nouveauType.name())
                .message("Félicitations ! Votre relevé a été validé. Niveau mis à jour : " + nouveauNiveau.getLabel())
                .build();
    }

    private String extraireTexte(MultipartFile file, String mimeType) {
        try {
            if (mimeType.contains("pdf")) {
                return pdfProcessingService.extractTextFromPdf(new ByteArrayInputStream(file.getBytes()));
            } else {
                return aiEmbeddingService.extractTextFromImage(file.getBytes(), mimeType);
            }
        } catch (Exception e) {
            log.error("Erreur extraction texte", e);
            return null;
        }
    }

    private JsonNode analyserAvecIA(String texte) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(chatApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = String.format(ANALYSE_PROMPT, texte);

            Map<String, Object> body = Map.of(
                    "model", chatModel,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 800,
                    "temperature", 0.1
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    chatUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            String content = objectMapper.readTree(response.getBody())
                    .path("choices").get(0).path("message").path("content").asText().strip();

            String json = content;
            if (json.startsWith("```json")) {
                json = json.substring(7, json.lastIndexOf("```")).strip();
            } else if (json.startsWith("```")) {
                json = json.substring(3, json.lastIndexOf("```")).strip();
            }

            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Erreur analyse IA, fallback regex: {}", e.getMessage());
            return null;
        }
    }

    private ReleveValidationResponse fallbackRegex(String texte, Eleve eleve, UUID eleveTrackingId,
                                                     MultipartFile file, String mimeType) {
        boolean estAdmis = texte.toUpperCase().contains("ADMIS");
        boolean estAjourne = texte.toUpperCase().contains("AJOURN") || texte.toUpperCase().contains("AJOURNE");
        boolean estBepc = texte.toUpperCase().contains("BEPC");
        boolean estBac = texte.toUpperCase().contains("BACCALAUREAT") || texte.toUpperCase().contains("BAC");
        boolean estBulletin = texte.toUpperCase().contains("BULLETIN");

        String typeDoc;
        if (estBepc) typeDoc = "RELEVE_BEPC";
        else if (estBac) typeDoc = "RELEVE_BAC";
        else if (estBulletin) typeDoc = "BULLETIN_TRIMESTRIEL";
        else typeDoc = "AUTRE";

        if (!estAdmis || estBulletin || typeDoc.equals("AUTRE")) {
            sauvegarderDocument(eleve, file);
            return ReleveValidationResponse.builder()
                    .trackingId(eleveTrackingId)
                    .valide(false)
                    .typeDocument(typeDoc)
                    .decision(estAjourne ? "AJOURNE" : estAdmis ? "ADMIS" : "NON_DETERMINE")
                    .message("Document non valide pour le changement de niveau.")
                    .raisonRejet(estBulletin ? "Les bulletins trimestriels ne sont pas acceptés" :
                            typeDoc.equals("AUTRE") ? "Document non reconnu" : "Décision : AJOURNÉ")
                    .build();
        }

        NiveauScolaire nouveauNiveau = determinerNiveau(eleve.getTypeApprenant(), typeDoc);
        if (nouveauNiveau == null) {
            sauvegarderDocument(eleve, file);
            return ReleveValidationResponse.builder()
                    .trackingId(eleveTrackingId)
                    .valide(false)
                    .typeDocument(typeDoc)
                    .decision("ADMIS")
                    .message("Votre profil actuel ne permet pas cette transition.")
                    .build();
        }

        TypeApprenant nouveauType = determinerTypeApprenant(eleve.getTypeApprenant(), typeDoc);
        eleve.setNiveau(nouveauNiveau);
        eleve.setTypeApprenant(nouveauType);
        eleveRepository.save(eleve);
        sauvegarderDocument(eleve, file);

        log.info("Niveau mis à jour (fallback) : eleve={} -> {}", eleveTrackingId, nouveauNiveau);

        return ReleveValidationResponse.builder()
                .trackingId(eleveTrackingId)
                .valide(true)
                .typeDocument(typeDoc)
                .decision("ADMIS")
                .niveauAttribue(nouveauNiveau.getLabel())
                .typeApprenantAttribue(nouveauType.name())
                .message("Félicitations ! Votre relevé a été validé. Niveau mis à jour : " + nouveauNiveau.getLabel())
                .build();
    }

    /**
     * Détermine le {@link NiveauScolaire} à attribuer à l'élève en fonction de
     * son type d'apprenant actuel et du type de document validé (BEPC / BAC).
     *
     * <p>Mapping togolais :</p>
     * <ul>
     *   <li>Collégien + relevé BEPC = Seconde (LYCEE_2ND)</li>
     *   <li>Lycéen + relevé BAC = Licence 1 (BAC_1)</li>
     * </ul>
     *
     * @return le nouveau niveau, ou {@code null} si la transition n'est pas
     *         applicable pour ce profil.
     */
    private NiveauScolaire determinerNiveau(TypeApprenant typeActuel, String typeDocument) {
        if (typeActuel == TypeApprenant.COLLEGIEN && "RELEVE_BEPC".equals(typeDocument)) {
            return NiveauScolaire.LYCEE_2ND;
        }
        if (typeActuel == TypeApprenant.LYCEEN && "RELEVE_BAC".equals(typeDocument)) {
            return NiveauScolaire.BAC_1;
        }
        return null;
    }

    private TypeApprenant determinerTypeApprenant(TypeApprenant typeActuel, String typeDocument) {
        if (typeActuel == TypeApprenant.COLLEGIEN && "RELEVE_BEPC".equals(typeDocument)) {
            return TypeApprenant.LYCEEN;
        }
        if (typeActuel == TypeApprenant.LYCEEN && "RELEVE_BAC".equals(typeDocument)) {
            return TypeApprenant.ETUDIANT;
        }
        return typeActuel;
    }

    private void sauvegarderDocument(Eleve eleve, MultipartFile file) {
        try {
            FileUploadResponse upload = minioService.uploadFile(file, FileType.DOCUMENT);
            Document document = Document.builder()
                    .urlFichier(upload.getFileUrl())
                    .nomFichier(file.getOriginalFilename())
                    .typeDocument(Document.TypeDocument.RELEVE_NOTES)
                    .dateDocument(LocalDate.now())
                    .tailleFichier(file.getSize())
                    .typeMime(file.getContentType())
                    .eleve(eleve)
                    .build();
            documentRepository.save(document);
            log.info("Relevé de notes sauvegardé : eleve={} fichier={}", eleve.getTrackingId(), upload.getFileName());
        } catch (Exception e) {
            log.error("Erreur sauvegarde document", e);
        }
    }
}
