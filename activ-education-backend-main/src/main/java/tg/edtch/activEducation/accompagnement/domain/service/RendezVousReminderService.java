package tg.edtch.activEducation.accompagnement.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous;
import tg.edtch.activEducation.accompagnement.repository.RendezVousRepository;
import tg.edtch.activEducation.shared.notification.SmsService;
import tg.edtch.activEducation.shared.util.AuditLogService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RendezVousReminderService {

    private final RendezVousRepository rendezVousRepository;
    private final SmsService smsService;
    private final AuditLogService auditLogService;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void rappelerRdvJour() {
        LocalDateTime debut = LocalDateTime.now();
        LocalDateTime fin = debut.plusHours(24);
        List<RendezVous> rdvs = rendezVousRepository.findByStatutAndDateHeurePrevueBetween(
                RendezVous.StatutRendezVous.PLANIFIE, debut, fin);

        for (RendezVous rdv : rdvs) {
            try {
                String telephone = rdv.getEleve().getTelephone();
                String prenomEleve = rdv.getEleve().getPrenom();
                String nomConseiller = rdv.getConseiller().getPrenom() + " " + rdv.getConseiller().getNom();
                String dateStr = rdv.getDateHeurePrevue().toLocalDate().toString();
                String heureStr = rdv.getDateHeurePrevue().toLocalTime().toString().substring(0, 5);

                String message = String.format(
                        "Bonjour %s, rappel: votre rendez-vous avec %s est prévu le %s à %s. - Activ Education",
                        prenomEleve, nomConseiller, dateStr, heureStr);

                smsService.envoyerSms(telephone, message);
                auditLogService.log(rdv.getEleve().getEmail(), prenomEleve,
                        "RAPPEL_RDV", "/api/v1/rendez-vous/" + rdv.getTrackingId(),
                        "Rappel envoyé par SMS à " + telephone,
                        "system", "RappelScheduler");

                log.info("Rappel RDV envoyé à {} ({}) pour rdv {}", prenomEleve, telephone, rdv.getTrackingId());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi du rappel pour rdv {}: {}", rdv.getTrackingId(), e.getMessage());
            }
        }

        if (rdvs.isEmpty()) {
            log.info("Aucun rappel RDV à envoyer aujourd'hui");
        } else {
            log.info("{} rappel(s) RDV envoyé(s)", rdvs.size());
        }
    }
}
