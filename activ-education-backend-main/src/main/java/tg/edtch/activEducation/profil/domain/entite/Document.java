package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDate;

/**
 * Entité représentant un document (bulletin, attestation...) rattaché à un
 * élève.
 * Conformément aux décisions V2 : pas d'OCR, stockage simple de la référence
 * (URL vers le stockage objet) avec métadonnées associées.
 */
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_document_eleve_id", columnList = "eleve_id"),
        @Index(name = "idx_document_type", columnList = "type_document")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * URL vers le fichier stocké (ex : Amazon S3, MinIO, Cloudinary...).
     */
    @Column(name = "url_fichier", nullable = false, length = 500)
    private String urlFichier;

    /**
     * Nom original du fichier tel que fourni par l'utilisateur.
     */
    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    /**
     * Type de document : BULLETIN, ATTESTATION, RELEVE_NOTES, AUTRE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false, length = 50)
    private TypeDocument typeDocument;

    /**
     * Date du document (ex : date du bulletin scolaire).
     */
    @Column(name = "date_document")
    private LocalDate dateDocument;

    /**
     * Description ou remarque optionnelle.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Taille du fichier en octets.
     */
    @Column(name = "taille_fichier")
    private Long tailleFichier;

    /**
     * Type MIME du fichier (ex : application/pdf, image/jpeg).
     */
    @Column(name = "type_mime", length = 100)
    private String typeMime;

    /**
     * Elève propriétaire du document.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    /**
     * Types de documents supportés.
     */
    public enum TypeDocument {
        BULLETIN,
        ATTESTATION,
        RELEVE_NOTES,
        CERTIFICAT_SCOLARITE,
        AUTRE
    }
}
