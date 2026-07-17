package tg.edtch.activEducation.shared.visio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class VisioService {

    @Value("${visio.provider:jitsi}")
    private String provider;

    @Value("${visio.jitsi.base-url:https://meet.jit.si}")
    private String jitsiBaseUrl;

    public String genererLienVisio() {
        String roomName = "ActivEducation-" + UUID.randomUUID().toString().substring(0, 8);
        String lien = switch (provider.toLowerCase()) {
            case "jitsi" -> jitsiBaseUrl + "/" + roomName;
            default -> jitsiBaseUrl + "/" + roomName;
        };
        log.info("Lien visio généré : {}", lien);
        return lien;
    }
}
