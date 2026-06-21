package tg.edtch.activEducation.shared.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sms.provider:none}")
    private String smsProvider;

    @Value("${sms.api-key:}")
    private String smsApiKey;

    @Value("${sms.sender:ActivEduc}")
    private String smsSender;

    public boolean envoyerSms(String telephone, String message) {
        if ("none".equals(smsProvider) || smsApiKey.isBlank()) {
            log.info("[SMS SIMULATED] À {}: {}", telephone, message);
            return true;
        }

        try {
            if ("twilio".equals(smsProvider)) {
                return envoyerViaTwilio(telephone, message);
            } else if ("orange".equals(smsProvider)) {
                return envoyerViaOrange(telephone, message);
            }
            log.warn("Fournisseur SMS '{}' non supporté", smsProvider);
            return false;
        } catch (Exception e) {
            log.error("Erreur envoi SMS vers {}: {}", telephone, e.getMessage());
            return false;
        }
    }

    private boolean envoyerViaTwilio(String telephone, String message) {
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + smsApiKey + "/Messages.json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String body = "To=" + telephone + "&From=" + smsSender + "&Body=" + message;
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
        return true;
    }

    private boolean envoyerViaOrange(String telephone, String message) {
        String url = "https://api.orange.com/smsmessaging/v1/outbound/tel%3A%2B" + smsSender + "/requests";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + smsApiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("address", "tel:" + telephone);
        payload.put("senderAddress", "tel:+" + smsSender);
        payload.put("outboundSMSTextMessage", Map.of("message", message));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(url, request, String.class);
        return true;
    }
}
