package tg.edtch.activEducation.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersioningService {

    private final VersionHistoriqueRepository versionRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerCreation(String itemType, String itemTrackingId, String whodunnit, Object objet) {
        VersionHistorique version = VersionHistorique.builder()
                .itemType(itemType).itemTrackingId(itemTrackingId)
                .event("create").whodunnit(whodunnit)
                .objectData(toJson(objet)).build();
        versionRepository.save(version);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerModification(String itemType, String itemTrackingId, String whodunnit,
                                        Object ancienEtat, Object nouvelEtat) {
        VersionHistorique version = VersionHistorique.builder()
                .itemType(itemType).itemTrackingId(itemTrackingId)
                .event("update").whodunnit(whodunnit)
                .objectData(toJson(nouvelEtat))
                .objectChanges(calculerChangements(ancienEtat, nouvelEtat)).build();
        versionRepository.save(version);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerSuppression(String itemType, String itemTrackingId, String whodunnit, Object objet) {
        VersionHistorique version = VersionHistorique.builder()
                .itemType(itemType).itemTrackingId(itemTrackingId)
                .event("delete").whodunnit(whodunnit)
                .objectData(toJson(objet)).build();
        versionRepository.save(version);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Erreur sérialisation versioning", e);
            return "{}";
        }
    }

    private String calculerChangements(Object ancien, Object nouveau) {
        try {
            Map<String, Object> ancienMap = objectMapper.convertValue(ancien, LinkedHashMap.class);
            Map<String, Object> nouveauMap = objectMapper.convertValue(nouveau, LinkedHashMap.class);
            Map<String, Object> changements = new LinkedHashMap<>();

            for (String key : nouveauMap.keySet()) {
                Object oldVal = ancienMap.get(key);
                Object newVal = nouveauMap.get(key);
                if (oldVal == null && newVal == null) continue;
                if (oldVal != null && oldVal.equals(newVal)) continue;
                if (oldVal == null || !oldVal.equals(newVal)) {
                    Map<String, Object> diff = new LinkedHashMap<>();
                    if (oldVal != null) diff.put("old", oldVal);
                    diff.put("new", newVal);
                    changements.put(key, diff);
                }
            }
            return objectMapper.writeValueAsString(changements);
        } catch (Exception e) {
            log.error("Erreur calcul changements versioning", e);
            return "{}";
        }
    }
}
