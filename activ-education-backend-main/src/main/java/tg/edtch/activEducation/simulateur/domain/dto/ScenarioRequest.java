package tg.edtch.activEducation.simulateur.domain.dto;

import java.util.List;
import java.util.Map;

public class ScenarioRequest {

    private String titre;
    private String serieTrackingId;
    private String niveau;
    private String ville;
    private String typeEtablissement;
    private Boolean etablissementPublic;
    private String motCleMetier;
    private Map<String, Double> notesSimulees;

    private List<String> scenariosATitres;

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getSerieTrackingId() { return serieTrackingId; }
    public void setSerieTrackingId(String serieTrackingId) { this.serieTrackingId = serieTrackingId; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getTypeEtablissement() { return typeEtablissement; }
    public void setTypeEtablissement(String typeEtablissement) { this.typeEtablissement = typeEtablissement; }
    public Boolean getEtablissementPublic() { return etablissementPublic; }
    public void setEtablissementPublic(Boolean etablissementPublic) { this.etablissementPublic = etablissementPublic; }
    public String getMotCleMetier() { return motCleMetier; }
    public void setMotCleMetier(String motCleMetier) { this.motCleMetier = motCleMetier; }
    public Map<String, Double> getNotesSimulees() { return notesSimulees; }
    public void setNotesSimulees(Map<String, Double> notesSimulees) { this.notesSimulees = notesSimulees; }
    public List<String> getScenariosATitres() { return scenariosATitres; }
    public void setScenariosATitres(List<String> scenariosATitres) { this.scenariosATitres = scenariosATitres; }
}
