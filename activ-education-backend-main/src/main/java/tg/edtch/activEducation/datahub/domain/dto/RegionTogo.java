package tg.edtch.activEducation.datahub.domain.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum RegionTogo {
    MARITIME("Maritime", "Région Maritime", 6.1319, 1.2225, "Lomé", "Tsévié", "Aneho", "Tabligbo"),
    PLATEAUX("Plateaux", "Région des Plateaux", 7.5167, 1.1167, "Atakpamé", "Kpalimé", "Notsé", "Badou"),
    CENTRALE("Centrale", "Région Centrale", 8.9833, 1.1333, "Sokodé", "Tchamba", "Sotouboua"),
    KARA("Kara", "Région de la Kara", 9.5500, 1.1833, "Kara", "Niamtougou", "Bafilo", "Pagouda"),
    SAVANES("Savanes", "Région des Savanes", 10.8667, 0.2000, "Dapaong", "Mango", "Cinkassé");

    private final String nom;
    private final String nomComplet;
    private final double latitude;
    private final double longitude;
    private final List<String> villes;

    RegionTogo(String nom, String nomComplet, double latitude, double longitude, String... villes) {
        this.nom = nom;
        this.nomComplet = nomComplet;
        this.latitude = latitude;
        this.longitude = longitude;
        this.villes = List.of(villes);
    }

    public String getNom() { return nom; }
    public String getNomComplet() { return nomComplet; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<String> getVilles() { return villes; }

    private static final Map<String, RegionTogo> VILLE_TO_REGION = Arrays.stream(values())
        .flatMap(r -> r.villes.stream().map(v -> Map.entry(v.toLowerCase(), r)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

    public static RegionTogo fromVille(String ville) {
        if (ville == null) return null;
        return VILLE_TO_REGION.get(ville.strip().toLowerCase());
    }
}
