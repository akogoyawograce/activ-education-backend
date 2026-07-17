package tg.edtch.activEducation.simulateur.domain.dto;

import java.util.List;
import java.util.Map;

public class ScenarioResult {

    private String titre;
    private String serieTitre;
    private List<FiliereMatch> filieres;
    private List<MetierCible> metiers;
    private List<EtablissementCible> etablissements;
    private StatsRecap stats;

    /**
     * Analyse comparative (Chantier A — v2 du simulateur). Null si
     * {@code explorer()} a été appelé directement ; non-null uniquement
     * quand {@code comparer()} a calculé un delta entre scénarios.
     */
    private ComparaisonAnalyse comparaison;

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getSerieTitre() { return serieTitre; }
    public void setSerieTitre(String serieTitre) { this.serieTitre = serieTitre; }
    public List<FiliereMatch> getFilieres() { return filieres; }
    public void setFilieres(List<FiliereMatch> filieres) { this.filieres = filieres; }
    public List<MetierCible> getMetiers() { return metiers; }
    public void setMetiers(List<MetierCible> metiers) { this.metiers = metiers; }
    public List<EtablissementCible> getEtablissements() { return etablissements; }
    public void setEtablissements(List<EtablissementCible> etablissements) { this.etablissements = etablissements; }
    public StatsRecap getStats() { return stats; }
    public void setStats(StatsRecap stats) { this.stats = stats; }
    public ComparaisonAnalyse getComparaison() { return comparaison; }
    public void setComparaison(ComparaisonAnalyse comparaison) { this.comparaison = comparaison; }

    public static class FiliereMatch {
        private String trackingId;
        private String titre;
        private String resume;
        private String domaine;
        private String duree;
        private String niveauRequis;
        private double scoreCompatibilite;
        private int seuilsValides;
        private int seuilsTotal;

        public String getTrackingId() { return trackingId; }
        public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public String getResume() { return resume; }
        public void setResume(String resume) { this.resume = resume; }
        public String getDomaine() { return domaine; }
        public void setDomaine(String domaine) { this.domaine = domaine; }
        public String getDuree() { return duree; }
        public void setDuree(String duree) { this.duree = duree; }
        public String getNiveauRequis() { return niveauRequis; }
        public void setNiveauRequis(String niveauRequis) { this.niveauRequis = niveauRequis; }
        public double getScoreCompatibilite() { return scoreCompatibilite; }
        public void setScoreCompatibilite(double scoreCompatibilite) { this.scoreCompatibilite = scoreCompatibilite; }
        public int getSeuilsValides() { return seuilsValides; }
        public void setSeuilsValides(int seuilsValides) { this.seuilsValides = seuilsValides; }
        public int getSeuilsTotal() { return seuilsTotal; }
        public void setSeuilsTotal(int seuilsTotal) { this.seuilsTotal = seuilsTotal; }
    }

    public static class MetierCible {
        private String trackingId;
        private String titre;
        private String resume;
        private String secteur;
        private String fourchetteSalaire;

        public String getTrackingId() { return trackingId; }
        public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public String getResume() { return resume; }
        public void setResume(String resume) { this.resume = resume; }
        public String getSecteur() { return secteur; }
        public void setSecteur(String secteur) { this.secteur = secteur; }
        public String getFourchetteSalaire() { return fourchetteSalaire; }
        public void setFourchetteSalaire(String fourchetteSalaire) { this.fourchetteSalaire = fourchetteSalaire; }
    }

    public static class EtablissementCible {
        private String trackingId;
        private String titre;
        private String ville;
        private String type;
        private String niveau;
        private Boolean estPublic;
        private List<String> filieresProposeesTitres;

        public String getTrackingId() { return trackingId; }
        public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public String getVille() { return ville; }
        public void setVille(String ville) { this.ville = ville; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getNiveau() { return niveau; }
        public void setNiveau(String niveau) { this.niveau = niveau; }
        public Boolean getEstPublic() { return estPublic; }
        public void setEstPublic(Boolean estPublic) { this.estPublic = estPublic; }
        public List<String> getFilieresProposeesTitres() { return filieresProposeesTitres; }
        public void setFilieresProposeesTitres(List<String> filieresProposeesTitres) { this.filieresProposeesTitres = filieresProposeesTitres; }
    }

    public static class StatsRecap {
        private int totalFilieres;
        private int totalMetiers;
        private int totalEtablissements;
        private double scoreMoyenCompatibilite;
        private double dureeMin;
        private double dureeMax;

        public int getTotalFilieres() { return totalFilieres; }
        public void setTotalFilieres(int totalFilieres) { this.totalFilieres = totalFilieres; }
        public int getTotalMetiers() { return totalMetiers; }
        public void setTotalMetiers(int totalMetiers) { this.totalMetiers = totalMetiers; }
        public int getTotalEtablissements() { return totalEtablissements; }
        public void setTotalEtablissements(int totalEtablissements) { this.totalEtablissements = totalEtablissements; }
        public double getScoreMoyenCompatibilite() { return scoreMoyenCompatibilite; }
        public void setScoreMoyenCompatibilite(double scoreMoyenCompatibilite) { this.scoreMoyenCompatibilite = scoreMoyenCompatibilite; }
        public double getDureeMin() { return dureeMin; }
        public void setDureeMin(double dureeMin) { this.dureeMin = dureeMin; }
        public double getDureeMax() { return dureeMax; }
        public void setDureeMax(double dureeMax) { this.dureeMax = dureeMax; }
    }

    /**
     * Synthèse comparative entre plusieurs scénarios (résultat de
     * {@code SimulateurParcoursService.comparer(...)}).
     *
     * <p>Le champ {@code deltasParFiliere} donne, pour chaque filière
     * présente dans au moins 2 scénarios, l'écart max-min observé sur
     * le score de compatibilité. Clé : titre de la filière. Valeur :
     * liste ordonnée par titre de scénario, avec le score obtenu.</p>
     */
    public static class ComparaisonAnalyse {
        private String meilleurScenario;
        private String pireScenario;
        private double scoreMoyenMax;
        private double scoreMoyenMin;
        private int nombreFilieresCommunes;
        private int nombreScenarios;
        private Map<String, List<DeltaParScenario>> deltasParFiliere;
        private String synthese;

        public String getMeilleurScenario() { return meilleurScenario; }
        public void setMeilleurScenario(String meilleurScenario) { this.meilleurScenario = meilleurScenario; }
        public String getPireScenario() { return pireScenario; }
        public void setPireScenario(String pireScenario) { this.pireScenario = pireScenario; }
        public double getScoreMoyenMax() { return scoreMoyenMax; }
        public void setScoreMoyenMax(double scoreMoyenMax) { this.scoreMoyenMax = scoreMoyenMax; }
        public double getScoreMoyenMin() { return scoreMoyenMin; }
        public void setScoreMoyenMin(double scoreMoyenMin) { this.scoreMoyenMin = scoreMoyenMin; }
        public int getNombreFilieresCommunes() { return nombreFilieresCommunes; }
        public void setNombreFilieresCommunes(int nombreFilieresCommunes) { this.nombreFilieresCommunes = nombreFilieresCommunes; }
        public int getNombreScenarios() { return nombreScenarios; }
        public void setNombreScenarios(int nombreScenarios) { this.nombreScenarios = nombreScenarios; }
        public Map<String, List<DeltaParScenario>> getDeltasParFiliere() { return deltasParFiliere; }
        public void setDeltasParFiliere(Map<String, List<DeltaParScenario>> deltasParFiliere) { this.deltasParFiliere = deltasParFiliere; }
        public String getSynthese() { return synthese; }
        public void setSynthese(String synthese) { this.synthese = synthese; }
    }

    /**
     * Score d'un scénario donné pour une filière commune.
     * Permet de visualiser, pour chaque filière, l'écart entre les scénarios.
     */
    public static class DeltaParScenario {
        private String scenarioTitre;
        private double score;

        public DeltaParScenario() { }

        public DeltaParScenario(String scenarioTitre, double score) {
            this.scenarioTitre = scenarioTitre;
            this.score = score;
        }

        public String getScenarioTitre() { return scenarioTitre; }
        public void setScenarioTitre(String scenarioTitre) { this.scenarioTitre = scenarioTitre; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}
