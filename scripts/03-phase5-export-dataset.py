"""
Phase 5 — Téléchargement du dataset depuis le backend Spring Boot.

Usage :
  python phase5_export_dataset.py --token <JWT> [--out real_dataset.csv]

Pré-requis :
  - Backend démarré : http://localhost:8080
  - Compte ADMIN ou SUPER_ADMIN (auth via JWT)
  - Au moins quelques orientation_outcome avec statut ADMIS ou RECALE
    (sinon le CSV ne contiendra que l'en-tête).
"""

import argparse
import sys
from pathlib import Path

import requests


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--base-url", default="http://localhost:8080",
                   help="URL du backend Spring Boot")
    p.add_argument("--token", required=True,
                   help="JWT de l'utilisateur ADMIN/SUPER_ADMIN")
    p.add_argument("--out", default="real_dataset.csv",
                   help="Fichier CSV de sortie")
    args = p.parse_args()

    url = f"{args.base_url}/api/v1/admin/prediction/dataset"
    headers = {"Authorization": f"Bearer {args.token}"}

    print(f"GET {url}…")
    resp = requests.get(url, headers=headers, timeout=60)

    if resp.status_code == 401:
        print("401 Unauthorized — token invalide ou expiré.", file=sys.stderr)
        sys.exit(1)
    if resp.status_code == 403:
        print("403 Forbidden — il faut un compte ADMIN/SUPER_ADMIN.", file=sys.stderr)
        sys.exit(1)
    if resp.status_code != 200:
        print(f"Erreur {resp.status_code} : {resp.text[:200]}", file=sys.stderr)
        sys.exit(1)

    out = Path(args.out)
    out.write_bytes(resp.content)

    n_lines = resp.content.decode("utf-8").count("\n")
    print(f"OK — {n_lines - 1} ligne(s) écrite(s) dans {out}")
    if n_lines <= 1:
        print(
            "⚠️  Dataset vide. Le backend n'a aucun orientation_outcome\n"
            "   avec statut ADMIS/RECALE. Continuer quand même n'aura pas\n"
            "   de sens. Accumuler d'abord des données réelles."
        )


if __name__ == "__main__":
    main()
