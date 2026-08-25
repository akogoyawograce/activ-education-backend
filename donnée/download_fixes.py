from pathlib import Path
import requests

base = Path('/home/ubuntu/collecte_togo/sources')
base.mkdir(parents=True, exist_ok=True)
urls = {
    'unesco_sdg_data_tgo.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/4ef60c4f-caad-4ece-bfb2-cc7425ecd432/download/sdg_data_tgo.csv',
    'unesco_sdg_indicatorlist_tgo.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/264a42b6-bd97-4f8e-a997-3fc083b8a51f/download/sdg_indicatorlist_tgo.csv',
    'unesco_opri_data_tgo.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/564d870e-3546-4237-b4f8-08e4e9950a39/download/opri_data_tgo.csv',
    'unesco_opri_indicatorlist_tgo.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/684b56c7-25bc-451c-b648-e38b028bd1e7/download/opri_indicatorlist_tgo.csv',
    'unesco_dem_data_tgo.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/776b1d36-d66a-4fa9-890d-77650c0f7a49/download/dem_data_tgo.csv',
    'unesco_dem_indicatorlist_tgo.csv': 'https://data.humdata.org/dataset/5a2f356e-398b-4696-b482-ed0c93cbfaac/resource/255d5635-5291-4330-82d4-884700500eb9/download/dem_indicatorlist_tgo.csv',
    'onet_career_interest_types.csv': 'https://www.onetcenter.org/dl_files/database/db_30_3_csv/career_interest_types.csv',
    'onet_specific_interest_areas.csv': 'https://www.onetcenter.org/dl_files/database/db_30_3_csv/specific_interest_areas.csv',
    'onet_specific_interest_mapping.csv': 'https://www.onetcenter.org/dl_files/database/db_30_3_csv/specific_interest_areas_to_career_interest_types.csv',
}
for name, url in urls.items():
    try:
        r = requests.get(url, timeout=120)
        r.raise_for_status()
        (base / name).write_bytes(r.content)
        print(f'{name}\t{len(r.content)} octets')
    except Exception as e:
        print(f'ECHEC\t{name}\t{e}')
