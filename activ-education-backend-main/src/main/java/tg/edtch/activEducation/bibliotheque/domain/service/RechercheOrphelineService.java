package tg.edtch.activEducation.bibliotheque.domain.service;

import java.util.Map;

public interface RechercheOrphelineService {
    void signaler(String terme, String module);

    Map<String, Long> getTermesFrequents(int limite);
}
