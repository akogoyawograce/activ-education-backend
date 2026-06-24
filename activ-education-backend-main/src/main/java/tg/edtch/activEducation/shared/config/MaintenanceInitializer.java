package tg.edtch.activEducation.shared.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tg.edtch.activEducation.shared.util.ParametreApplicationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceInitializer {

    private final ParametreApplicationRepository parametreRepository;

    @PostConstruct
    public void init() {
        parametreRepository.findByCle("maintenance.enabled").ifPresent(param -> {
            boolean enabled = Boolean.parseBoolean(param.getValeur());
            MaintenanceFilter.setMaintenanceMode(enabled);
            log.info("Mode maintenance chargé depuis la DB : {}", enabled);
        });
        parametreRepository.findByCle("maintenance.message").ifPresent(param -> {
            MaintenanceFilter.setMaintenanceMessage(param.getValeur());
            log.info("Message de maintenance chargé depuis la DB");
        });
    }
}
