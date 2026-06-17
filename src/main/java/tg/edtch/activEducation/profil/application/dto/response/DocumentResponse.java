package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String urlFichier;
    private String nomFichier;
    private String typeDocument;
    private LocalDate dateDocument;
    private String description;
    private Long tailleFichier;
    private String typeMime;
    private LocalDateTime createdAt;
}
