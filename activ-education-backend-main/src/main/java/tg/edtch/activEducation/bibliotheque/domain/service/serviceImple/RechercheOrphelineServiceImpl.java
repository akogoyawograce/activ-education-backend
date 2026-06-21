package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.RechercheOrpheline;
import tg.edtch.activEducation.bibliotheque.repository.RechercheOrphelineRepository;
import tg.edtch.activEducation.bibliotheque.domain.service.RechercheOrphelineService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechercheOrphelineServiceImpl implements RechercheOrphelineService {

    private final RechercheOrphelineRepository orphelineRepository;

    @Async
    @Override
    @Transactional
    public void signaler(String terme, String module) {
        if (terme == null || terme.trim().isEmpty()) {
            return;
        }

        log.warn("Recherche orpheline détectée : [{}] dans le module [{}]", terme, module);
        RechercheOrpheline entity = RechercheOrpheline.builder()
                .terme(terme.trim().toLowerCase())
                .module(module)
                .build();
        orphelineRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTermesFrequents(int limite) {
        List<Object[]> resultats = orphelineRepository.trouverTermesLesPlusFrequents(PageRequest.of(0, limite));
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] ligne : resultats) {
            String terme = (String) ligne[0];
            Long count = (Long) ligne[1];
            map.put(terme, count);
        }
        return map;
    }
}
