package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.EntreeFAQ;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntreeFAQRepository extends JpaRepository<EntreeFAQ, Long> {

  Optional<EntreeFAQ> findByTrackingId(UUID trackingId);

  Page<EntreeFAQ> findAllByEstPublieTrue(Pageable pageable);

  List<EntreeFAQ> findByCategorieAndEstPublieTrue(String categorie);

  boolean existsByQuestion(String question);

  /**
   * Recherche sémantique par similarité cosinus (opérateur {@code <=>} pgvector).
   * Retourne les N entrées FAQ les plus proches du vecteur de requête.
   */
  @Query(value = """
      SELECT * FROM entrees_faq
      WHERE est_publie = true AND embedding IS NOT NULL
      ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
      LIMIT :limite
      """, nativeQuery = true)
  List<EntreeFAQ> rechercherParSimilarite(
      @Param("queryEmbedding") float[] queryEmbedding,
      @Param("limite") int limite);

  /**
   * Recherche sémantique filtrée par catégorie.
   */
  @Query(value = """
      SELECT * FROM entrees_faq
      WHERE est_publie = true AND embedding IS NOT NULL AND categorie = :categorie
      ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
      LIMIT :limite
      """, nativeQuery = true)
  List<EntreeFAQ> rechercherParSimilariteEtCategorie(
      @Param("queryEmbedding") float[] queryEmbedding,
      @Param("categorie") String categorie,
      @Param("limite") int limite);

  /**
   * Recherche sémantique avec seuil de distance cosinus.
   */
  @Query(value = """
      SELECT * FROM entrees_faq
      WHERE est_publie = true AND embedding IS NOT NULL
        AND (embedding <=> CAST(:queryEmbedding AS vector)) < :seuilDistance
      ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
      LIMIT :limite
      """, nativeQuery = true)
  List<EntreeFAQ> rechercherParSimilariteAvecSeuil(
      @Param("queryEmbedding") float[] queryEmbedding,
      @Param("seuilDistance") double seuilDistance,
      @Param("limite") int limite);

  @Query("SELECT f FROM EntreeFAQ f WHERE f.embedding IS NULL AND f.estPublie = true")
  List<EntreeFAQ> findAllSansEmbedding();

  @Query("SELECT DISTINCT f.categorie FROM EntreeFAQ f WHERE f.categorie IS NOT NULL AND f.estPublie = true ORDER BY f.categorie")
  List<String> findAllCategories();
}
