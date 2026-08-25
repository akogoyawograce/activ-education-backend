from pathlib import Path
import json
import csv
import zipfile
import shutil

base = Path('/home/ubuntu/collecte_togo')
src = base / 'sources'
out = base / 'structured'
out.mkdir(exist_ok=True)

# Extract selected O*NET files from the complete CSV package.
with zipfile.ZipFile(src / 'onet_30_3_csv.zip') as z:
    for member in ['db_30_3_csv/occupation_data.csv', 'db_30_3_csv/career_interest_types.csv', 'db_30_3_csv/specific_interest_areas.csv', 'db_30_3_csv/specific_interest_areas_to_career_interest_types.csv']:
        target = out / Path(member).name
        with z.open(member) as f, target.open('wb') as g:
            shutil.copyfileobj(f, g)

# Summarize CSV files with row counts and headers.
summary = []
for p in sorted(list(src.glob('*.csv')) + list(out.glob('*.csv'))):
    try:
        with p.open('r', encoding='utf-8-sig', errors='replace', newline='') as f:
            reader = csv.reader(f)
            header = next(reader, [])
            rows = sum(1 for _ in reader)
        summary.append({'file': str(p.relative_to(base)), 'rows': rows, 'columns': len(header), 'header': header[:30]})
    except Exception as exc:
        summary.append({'file': str(p.relative_to(base)), 'error': str(exc)})

for p in sorted(src.glob('worldbank_*.json')):
    try:
        data = json.loads(p.read_text())
        records = data[1] if isinstance(data, list) and len(data) > 1 else []
        summary.append({'file': str(p.relative_to(base)), 'rows': len(records), 'columns': len(records[0]) if records else 0, 'header': list(records[0].keys()) if records else []})
    except Exception as exc:
        summary.append({'file': str(p.relative_to(base)), 'error': str(exc)})

(base / 'source_summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2))
with (base / 'source_summary.tsv').open('w', encoding='utf-8') as f:
    f.write('fichier\tlignes\tcolonnes\tcolonnes_principales\n')
    for x in summary:
        f.write(f"{x['file']}\t{x.get('rows','')}\t{x.get('columns','')}\t{';'.join(x.get('header',[]))}\n")
print((base / 'source_summary.tsv').read_text())
