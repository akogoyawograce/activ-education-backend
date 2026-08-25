package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Détails supplémentaires d'un établissement (enrichis depuis le XLSX
 * national 08/2026). Conçu pour être inclus dans
 * {@link FicheEtablissementResponse} sous forme de bloc optionnel.
 *
 * Les champs sont volontairement nullable : un établissement non couvert par
 * le XLSX renverra simplement ce bloc avec des null.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementDetailsSupplementaires {

    /** Identité */
    private String sigle;
    private Integer anneeCreation;
    private String statutJuridique;
    private String numeroAgrement;
    private String dateAgrement;
    private String autoriteTutelle;
    private Boolean reconnaissanceCames;

    /** Localisation administrative */
    private String region;
    private String prefecture;
    private String commune;
    private String quartier;
    private String lienGoogleMaps;

    /** Présentation */
    private String presentation;
    private String mission;
    private String vision;
    private String valeurs;

    /** Frais */
    private String fraisInscription;
    private String scolariteAnnuelle;
    private String fraisDevise;

    /** Infrastructures (booléens + synthèse) */
    private String infrastructuresSynthese;
    private Integer nbBatiments;
    private Integer nbAmphitheatres;
    private Boolean bibliotheque;
    private Boolean wifi;
    private Boolean internat;
    private Boolean restaurant;
    private Boolean cafeteria;
    private Boolean terrainSport;
    private Boolean infirmerie;
    private Boolean parking;

    /** Personnes & stats */
    private Integer nbEnseignants;
    private Integer nbEtudiants;
    private Integer nbDiplomes;
    private String tauxReussite;
    private String tauxInsertionPro;

    /** Partenariats & bourses */
    private String universitesPartenaires;
    private String entreprisesPartenaires;
    private Boolean programmesMobilite;
    private String boursesInternes;
    private String boursesGouvernementales;
    private String boursesInternationales;

    /** Vie étudiante */
    private String clubs;
    private String sports;

    /** Avis */
    private Double noteMoyenneAvis;
    private Integer nbAvis;
}
