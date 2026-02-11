# Memo local - Monitoring Grafana/Loki

Date: 2026-02-10
Projet: item-api
Contexte: mise en place d'un script de monitoring pilotable

## Objectif
Pouvoir lancer des requêtes de monitoring depuis le repo pour analyser:
- logs applicatifs (Loki)
- symptômes réseau (timeouts, connection reset, 5xx, etc.)

## Fichiers créés
- scripts/monitoring/monitoring.ps1
- scripts/monitoring/README.md
- scripts/monitoring/.env.example

## Paramètres attendus
Variables d'environnement:
- GRAFANA_URL
- GRAFANA_TOKEN
- GRAFANA_LOKI_DS_UID

## Modes disponibles dans monitoring.ps1
- logs: requête brute Loki sur un selector
- errors: requête Loki avec filtre regex orienté erreurs réseau

Regex par défaut utilisée (mode errors):
(timeout|timed out|connection reset|broken pipe|refused|503|504|502)

## Exemple d'exécution
PowerShell:
$env:GRAFANA_URL="https://grafana.example.com"
$env:GRAFANA_TOKEN="glsa_xxx"
$env:GRAFANA_LOKI_DS_UID="loki_uid"
.\scripts\monitoring\monitoring.ps1 -Mode errors -LogSelector '{app="item-api"}'

## Points validés pendant l'échange
- Codex CLI peut exécuter des scripts locaux pour interroger une API de monitoring.
- Les secrets doivent rester en variables d'environnement (pas en dur dans les fichiers).
- Le dossier recommandé est scripts/monitoring dans le repo.
- Une version Grafana Loki du script a été fournie et installée.
- Possibilité d'ajouter ensuite un script métriques (Prometheus/Grafana) pour performances réseau fines.

## Ticket SOA-477 (synthèse rapide évoquée)
- Le changement de regex permet de reconnaître des lignes avec PPN vide au format ;rcr;epn.
- La structure du fichier résultat reste ppn;rcr;epn;retour.
- Libellé actuel en cas d'inexistence: "Exemplaire inexistant" (singulier).

## Actions possibles ensuite
1) Configurer les variables d'environnement réelles.
2) Lancer le script et exporter les résultats JSON.
3) Ajouter un script complémentaire métriques pour latence/erreurs réseau agrégées.
