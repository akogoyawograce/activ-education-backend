from pathlib import Path
import csv
import json

base = Path('/home/ubuntu/collecte_togo')
src = base / 'sources'
out = base / 'structured'
out.mkdir(exist_ok=True)

for stem in ['sdg', 'opri', 'dem']:
    data_path = src / f'unesco_{stem}_data_tgo.csv'
    labels_path = src / f'unesco_{stem}_indicatorlist_tgo.csv'
    labels = {}
    with labels_path.open(encoding='utf-8-sig', errors='replace') as f:
        for row in csv.DictReader(f):
            labels[row.get('indicator_id','')] = row.get('indicator_label_en','')
    rows = []
    with data_path.open(encoding='utf-8-sig', errors='replace') as f:
        for row in csv.DictReader(f):
            label = labels.get(row.get('indicator_id',''), '')
            if any(term in label.lower() for term in ['tertiary', 'higher education', 'completion', 'enrol', 'graduat', 'education expenditure', 'unemployment']):
                row['indicator_label_en'] = label
                rows.append(row)
    with (out / f'unesco_{stem}_education_indicators.csv').open('w', encoding='utf-8', newline='') as f:
        fields = ['indicator_id','indicator_label_en','country_id','year','value','magnitude','qualifier']
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader(); w.writerows(rows)
    print(stem, len(rows))

manifest = {
    'national': [
        {'name':'INSEED Annuaire statistique national 2024','status':'downloaded','role':'aggregates and higher-education indicators'},
        {'name':'Ministère Annuaire national 2024-2025','status':'downloaded','role':'BAC rates, school counts and education statistics'},
        {'name':'Université de Lomé concours PDFs','status':'downloaded','role':'program and admission-requirement reference; not admission outcomes'},
        {'name':'Togo Open Data portal','status':'catalogued','role':'155 education datasets and 22 employment datasets visible; individual exports still need selection'},
        {'name':'Official examination results portal','status':'found','role':'individual consultation portal; bulk export not confirmed'},
        {'name':'Université de Kara admission lists','status':'not found in accessible search','role':'required for real ADMIS/RECALE labels'},
        {'name':'MESR/ONEF/ANPE detailed employment and capacity tables','status':'not yet downloaded','role':'recommendation and labor-market enrichment'},
    ],
    'international': [
        {'name':'UNESCO UIS via HDX for Togo','status':'downloaded','role':'7,024 SDG rows, 18,579 policy rows, 1,687 demographic rows before filtering'},
        {'name':'World Bank WDI education indicators for Togo','status':'downloaded','role':'contextual time series; not individual outcomes'},
        {'name':'HOT/HDX education facilities GeoJSON','status':'downloaded','role':'geographic reference of education facilities; crowd-sourced caveat'},
        {'name':'O*NET 30.3','status':'downloaded','role':'occupation, interests and skills reference'},
        {'name':'ESCO','status':'not downloaded','role':'download requires selecting package and email delivery'},
        {'name':'PASEC/MICS/DHS microdata','status':'not downloaded','role':'access conditions and research-use restrictions must be checked'},
    ],
}
(base / 'sources_manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2))
(base / 'sources_manifest.md').write_text('''# Manifeste de collecte\n\nLes sources téléchargées sont classées selon leur rôle. Les données agrégées et les référentiels ne remplacent pas les outcomes individuels `ADMIS`/`RECALE`.\n\n## Sources collectées\n\n| Source | État | Rôle |\n|---|---|---|\n| INSEED Annuaire 2024 | Téléchargé | Agrégats et indicateurs du supérieur |\n| Ministère Annuaire 2024–2025 | Téléchargé | Taux BAC, établissements et statistiques scolaires |\n| Université de Lomé | Téléchargé | Référentiel de concours et filières, pas résultats |\n| UNESCO UIS via HDX | Téléchargé | Séries internationales pour le Togo |\n| Banque mondiale WDI | Téléchargé | Séries contextuelles |\n| HOT/HDX établissements | Téléchargé | Référentiel géographique, couverture non exhaustive |\n| O*NET 30.3 | Téléchargé | Métiers, intérêts et compétences |\n\n## Sources encore manquantes\n\nLes résultats individuels ou tableaux complets des concours de l’Université de Kara, des écoles privées et de l’Office du Baccalauréat restent nécessaires pour créer les labels réels. Les données détaillées MESR/ONEF/ANPE, ESCO et les microdonnées PASEC/MICS/DHS doivent également faire l’objet d’un accès ou d’un téléchargement séparé.\n''')
