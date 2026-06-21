package tg.edtch.activEducation.shared.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tg.edtch.activEducation.profil.domain.entite.Notification;
import tg.edtch.activEducation.profil.repository.NotificationRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPushService {

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${push.fcm.server-key:}")
    private String fcmServerKey;

    public Notification envoyerNotification(Long utilisateurId, String titre, String message) {
        Notification notification = Notification.builder()
                .titre(titre).message(message).lue(false)
                .build();
        notification = notificationRepository.save(notification);

        if (!fcmServerKey.isBlank()) {
            try {
                envoyerFCM(utilisateurId, titre, message);
            } catch (Exception e) {
                log.warn("FCM push failed for user={}: {}", utilisateurId, e.getMessage());
            }
        }

        log.info("Notification envoyée à user={}: {}", utilisateurId, titre);
        return notification;
    }

    private void envoyerFCM(Long utilisateurId, String titre, String message) {
        String url = "https://fcm.googleapis.com/fcm/send";

        Map<String, Object> notificationPayload = new HashMap<>();
        notificationPayload.put("title", titre);
        notificationPayload.put("body", message);

        Map<String, Object> data = new HashMap<>();
        data.put("to", "/users/" + utilisateurId);
        data.put("notification", notificationPayload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + fcmServerKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        restTemplate.postForEntity(url, request, String.class);
    }
}
