package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Document;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByEleveId(Long eleveId, Pageable pageable);

    List<Document> findByEleveIdAndTypeDocument(Long eleveId, Document.TypeDocument typeDocument);

    Page<Document> findByEleveIdAndTypeDocument(Long eleveId, Document.TypeDocument typeDocument, Pageable pageable);

    long countByEleveId(Long eleveId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Document d WHERE d.urlFichier = :url AND d.eleve.id = :eleveId")
    boolean existsByUrlFichierAndEleveId(@Param("url") String urlFichier, @Param("eleveId") Long eleveId);
}
