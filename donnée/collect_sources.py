from pathlib import Path
import json
import requests

BASE = Path('/home/ubuntu/collecte_togo/sources')
BASE.mkdir(parents=True, exist_ok=True)


def download(url: str, path: Path):
    r = requests.get(url, timeout=90)
    r.raise_for_status()
    path.write_bytes(r.content)
    print(f'{path.name}\t{len(r.content)} octets\t{url}')

# HDX / UNESCO UIS data for Togo, resources identified from the official dataset page.
hdx_resources = {
    'unesco_togo_sdg4.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/4ef60c4f-caad-4ece-bfb2-cc7425ecd432/download/unesco_togo_sdg4.csv',
    'unesco_togo_policy.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/564d870e-3546-4237-b4f8-08e4e9950a39/download/unesco_togo_policy.csv',
    'unesco_togo_demographic.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/776b1d36-d66a-4fa9-890d-77650c0f7a49/download/unesco_togo_demographic.csv',
}

# HDX / HOT education facilities.
hdx_geo = {
    'togo_education_facilities_geojson.zip': 'https://production-raw-data-api.s3.amazonaws.com/ISO3/TGO/education_facilities/hotosm_tgo_education_facilities_osm_geojson.zip',
    'togo_education_facilities_metadata.json': 'https://production-raw-data-api.s3.amazonaws.com/ISO3/TGO/education_facilities/hotosm_tgo_education_facilities_osm_metadata.json',
}

# O*NET 30.3 CSV package and selected RIASEC-related files.
onet = {
    'onet_30_3_csv.zip': 'https://www.onetcenter.org/dl_files/database/db_30_3_csv.zip',
    'onet_interests.csv': 'https://www.onetcenter.org/dl_files/database/db_30_3_csv/interests.csv',
    'onet_occupations.csv': 'https://www.onetcenter.org/dl_files/database/db_30_3_csv/occupation_data.csv',
}

# World Bank WDI indicators for Togo. Values are contextual, not individual outcomes.
indicators = {
    'SE.TER.ENRL': 'tertiary_enrollment',
    'SE.TER.CMPL.ZS': 'tertiary_completion',
    'SE.XPD.TOTL.GD.ZS': 'education_expenditure_gdp',
    'SL.UEM.1524.ZS': 'youth_unemployment',
}
for code, label in indicators.items():
    url = f'https://api.worldbank.org/v2/country/TGO/indicator/{code}?format=json&per_page=1000'
    try:
        download(url, BASE / f'worldbank_{label}.json')
    except Exception as exc:
        print(f'ECHEC\t{label}\t{exc}')

for group in (hdx_resources, hdx_geo, onet):
    for name, url in group.items():
        try:
            download(url, BASE / name)
        except Exception as exc:
            print(f'ECHEC\t{name}\t{exc}')
