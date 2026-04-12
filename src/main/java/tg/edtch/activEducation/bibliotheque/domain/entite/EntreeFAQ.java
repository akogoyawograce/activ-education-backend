package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Entité représentant une entrée de la FAQ (Foire Aux Questions).
 *
 * <p>
 * <strong>Recherche sémantique (CDC V2) :</strong>
 * Le champ {@code embedding} stocke le vecteur de représentation sémantique
 * de la question+réponse, généré par un modèle d'IA (ex :
 * text-embedding-3-small
 * d'OpenAI, dimension 1536). Ce vecteur est utilisé pour la recherche par
 * similarité cosinus via l'extension PostgreSQL {@code pgvector}.
 *
 * <p>
 * <strong>Prérequis PostgreSQL :</strong>
 * 
 * <pre>
 *   CREATE EXTENSION IF NOT EXISTS vector;
 * </pre>
 *
 * <p>
 * <strong>Index de performance (à exécuter manuellement) :</strong>
 * 
 * <pre>
 *   -- Index IVFFLAT pour une recherche approximative rapide (recommandé)
 *   CREATE INDEX idx_entree_faq_embedding
 *     ON entrees_faq
 *     USING ivfflat (embedding vector_cosine_ops)
 *     WITH (lists = 100);
 * </pre>
 */
@Entity
@Table(name = "entrees_faq", indexes = {
        @Index(name = "idx_faq_categorie", columnList = "categorie"),
        @Index(name = "idx_faq_est_publie", columnList = "est_publie")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EntreeFAQ extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * La question posée dans la FAQ.
     */
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    /**
     * La réponse officielle à la question.
     */
    @Column(name = "reponse", nullable = false, columnDefinition = "TEXT")
    private String reponse;

    /**
     * Catégorie thématique : ex. "Orientation", "Inscription", "Bourse", "Métiers".
     */
    @Column(name = "categorie", length = 100)
    private String categorie;

    /**
     * Indique si cette entrée FAQ est visible par les utilisateurs.
     */
    @Column(name = "est_publie", nullable = false)
    @Builder.Default
    private Boolean estPublie = false;

    /**
     * Nombre de vues / consultations de cette entrée.
     */
    @Column(name = "nb_vues")
    @Builder.Default
    private Long nbVues = 0L;

    /**
     * Vecteur d'embedding sémantique généré par un modèle IA.
     *
     * <p>
     * Utilise le type natif {@code vector(1536)} de pgvector via
     * {@code @JdbcTypeCode(SqlTypes.VECTOR)} et {@code @Array(length = 1536)}
     * d'Hibernate 6+. La dimension 1536 correspond à text-embedding-3-small
     * (OpenAI).
     * Adapter la dimension si un autre modèle est utilisé.
     *
     * <p>
     * <strong>Cycle de vie :</strong> ce champ est populé de façon asynchrone
     * par le service d'IA après la création/modification de l'entrée FAQ.
     * Il peut donc être {@code null} temporairement.
     */
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;
}
