package tg.edtch.activEducation.riasec.domain.dto;
public record RIASECResultatResponse(String trackingId, String eleveTrackingId, String codeProfil, String titres, Integer scoreRealiste, Integer scoreInvestigateur, Integer scoreArtistique, Integer scoreSocial, Integer scoreEntreprenant, Integer scoreConventionnel, String suggestionsMetiers, String datePassation) {}
