package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.service.OcrService;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewBulletinResponse {

    private UUID documentTrackingId;

    private List<OcrService.NoteExtraite> notesExtraites;

    private Periode periode;

    private String anneeScolaire;

    private String semestreOuTrimestre;

    private String message;
}
