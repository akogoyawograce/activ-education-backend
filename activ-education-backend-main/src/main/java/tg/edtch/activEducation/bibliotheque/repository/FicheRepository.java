package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheRepository extends JpaRepository<Fiche, Long> {

        @Query("SELECT f FROM Fiche f ORDER BY f.updatedAt DESC")
        List<Fiche> findTopByOrderByUpdatedAtDesc(Pageable pageable);

        /** trackingId reste UUID (identifiant public, pas la PK). */
        Optional<Fiche> findByTrackingId(UUID trackingId);

        Page<Fiche> findAllByEstPublieTrue(Pageable pageable);

        /**
         * Recherche par mot-clé étendue aux colonnes des sous-classes JOINED
         * (ville, type_etablissement, domaine filière) avec normalisation
         * d'accents via l'extension PostgreSQL unaccent (créée au démarrage
         * si absente — cf. migration V0).
         *
         * Avant le 6 août 2026, la requête ne touchait que titre/resume/contenu
         * de la table parente — d'où 0 résultat sur 'Lomé' (ville) ou 'informatique'
         * (domaine filière). Cf. JOURNAL_BORD_IA.md 6 août §2.
         *
         * Étape 1 : retourne les IDs matchant (en SQL natif avec unaccent).
         * Étape 2 : réhydrate les entités polymorphes via trouverParIdsOrdonnes
         * (même pattern que rechercherIdsParSimilariteGlobale pour contourner
         * le bug Hibernate JOINED + SQL natif où la colonne 'clazz_' manque).
         */
        @Query(value = "SELECT DISTINCT f.id FROM fiches f " +
                        "LEFT JOIN fiches_etablissement fe ON fe.id = f.id " +
                        "LEFT JOIN fiches_filiere ff ON ff.id = f.id " +
                        "WHERE f.est_publie = true AND " +
                        "(unaccent(LOWER(f.titre)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(f.resume)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(f.contenu)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(fe.ville)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(fe.type_etablissement)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(ff.domaine)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))))",
                countQuery = "SELECT COUNT(DISTINCT f.id) FROM fiches f " +
                        "LEFT JOIN fiches_etablissement fe ON fe.id = f.id " +
                        "LEFT JOIN fiches_filiere ff ON ff.id = f.id " +
                        "WHERE f.est_publie = true AND " +
                        "(unaccent(LOWER(f.titre)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(f.resume)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(f.contenu)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(fe.ville)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(fe.type_etablissement)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))) OR " +
                        " unaccent(LOWER(ff.domaine)) LIKE unaccent(LOWER(CONCAT('%', :terme, '%'))))",
                nativeQuery = true)
        List<Long> rechercherIdsParMotCle(@Param("terme") String terme);

        /**
         * Helper compatible avec l'ancienne signature (Pageable, utilisée par
         * OriaService) : applique la recherche et réhydrate via
         * trouverParIdsOrdonnes. Le Pageable sert uniquement à limiter.
         *
         * ⚠️ trouverParIdsOrdonnes a une ORDER BY CASE WHEN :ids.get(N) fixe
         * pour N=0..9. Si on a moins de 10 IDs, on contourne avec un findAllById
         * simple (ordre moins pertinent mais pas d'IndexOutOfBounds).
         */
        default Page<Fiche> rechercherParMotCle(String terme, Pageable pageable) {
                if (terme == null || terme.isBlank()) {
                        return new org.springframework.data.domain.PageImpl<>(java.util.List.of(), pageable, 0);
                }
                var ids = rechercherIdsParMotCle(terme);
                if (ids.isEmpty()) {
                        return new org.springframework.data.domain.PageImpl<>(java.util.List.of(), pageable, 0);
                }
                int limit = Math.min(ids.size(), pageable.getPageSize());
                var idsLimites = ids.subList(0, limit);
                java.util.List<Fiche> fiches;
                if (idsLimites.size() < 10) {
                    // Pas assez d'IDs pour le ORDER BY CASE de trouverParIdsOrdonnes
                    fiches = findAllById(idsLimites);
                } else {
                    fiches = trouverParIdsOrdonnes(idsLimites);
                }
                return new org.springframework.data.domain.PageImpl<>(fiches, pageable, ids.size());
        }

        @Modifying
        @Query("UPDATE Fiche f SET f.nbConsultations = f.nbConsultations + 1 WHERE f.id = :id")
        void incrementerConsultations(@Param("id") Long id);

        /**
         * Étape 1 : Récupère les IDs des fiches les plus proches via pgvector (requête
         * native).
         * On retourne des Long pour contourner le bug Hibernate InheritanceType.JOINED
         * avec les requêtes natives (colonne discriminante 'clazz_' absente).
         */
        @Query(value = "SELECT f.id FROM fiches f WHERE f.est_publie = true AND f.embedding IS NOT NULL ORDER BY f.embedding <=> CAST(:vecteur AS vector) LIMIT :limite", nativeQuery = true)
        List<Long> rechercherIdsParSimilariteGlobale(@Param("vecteur") String vecteur, @Param("limite") int limite);

        /**
         * Étape 2 : Charge les entités polymorphes dans le bon ordre à partir des IDs.
         * Le ORDER BY CASE préserve l'ordre de pertinence retourné par pgvector.
         */
        @Query("SELECT f FROM Fiche f WHERE f.id IN :ids ORDER BY CASE f.id "
                        + "WHEN :#{#ids.get(0)} THEN 0 "
                        + "WHEN :#{#ids.get(1)} THEN 1 "
                        + "WHEN :#{#ids.get(2)} THEN 2 "
                        + "WHEN :#{#ids.get(3)} THEN 3 "
                        + "WHEN :#{#ids.get(4)} THEN 4 "
                        + "WHEN :#{#ids.get(5)} THEN 5 "
                        + "WHEN :#{#ids.get(6)} THEN 6 "
                        + "WHEN :#{#ids.get(7)} THEN 7 "
                        + "WHEN :#{#ids.get(8)} THEN 8 "
                        + "WHEN :#{#ids.get(9)} THEN 9 "
                        + "ELSE 99 END")
        List<Fiche> trouverParIdsOrdonnes(@Param("ids") List<Long> ids);

        /**
         * Tendance sur 7 jours basée sur l'historique de consultation.
         */
        @Query(value = "SELECT f.id FROM fiches f " +
                        "JOIN historique_utilisateur h ON h.details = CAST(f.tracking_id AS text) " +
                        "WHERE h.action = 'CONSULTATION_FICHE' " +
                        "AND h.created_at >= NOW() - INTERVAL '7 days' " +
                        "GROUP BY f.id " +
                        "ORDER BY COUNT(h.id) DESC " +
                        "LIMIT :limite", nativeQuery = true)
        List<Long> trouverIdsTendances(@Param("limite") int limite);

        /**
         * Vues récentes (Historique) par un utilisateur spécifique.
         */
        @Query(value = "SELECT f.id FROM fiches f " +
                        "JOIN historique_utilisateur h ON h.details = CAST(f.tracking_id AS text) " +
                        "JOIN utilisateurs u ON h.utilisateur_id = u.id " +
                        "WHERE h.action = 'CONSULTATION_FICHE' " +
                        "AND u.tracking_id = :utilisateurTrackingId " +
                        "GROUP BY f.id " +
                        "ORDER BY MAX(h.created_at) DESC " +
                        "LIMIT :limite", nativeQuery = true)
        List<Long> trouverIdsConsultationsRecentes(@Param("utilisateurTrackingId") UUID utilisateurTrackingId,
                        @Param("limite") int limite);

        /**
         * Recherche de fiches similaires en utilisant les embeddings pgvector, tout en
         * excluant la fiche cible.
         */
        @Query(value = "SELECT f.id FROM fiches f WHERE f.id != :ficheId AND f.est_publie = true AND f.embedding IS NOT NULL ORDER BY f.embedding <=> CAST(:vecteur AS vector) LIMIT :limite", nativeQuery = true)
        List<Long> trouverIdsFichesSimilaires(@Param("ficheId") Long ficheId, @Param("vecteur") float[] vecteur,
                        @Param("limite") int limite);
}
