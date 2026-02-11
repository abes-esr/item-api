# Monitoring Script (Grafana Loki)

## Files
- `monitoring.ps1`: Query Grafana Loki via `/api/ds/query`.
- `.env.example`: Required environment variables.

## Setup
```powershell
$env:GRAFANA_URL="https://grafana.example.com"
$env:GRAFANA_TOKEN="glsa_xxx"
$env:GRAFANA_LOKI_DS_UID="loki_uid"
```

## Usage
```powershell
# Network-related errors in last 2h
.\scripts\monitoring\monitoring.ps1 -Mode errors -LogSelector '{app="item-api"}'

# Raw logs
.\scripts\monitoring\monitoring.ps1 -Mode logs -LogSelector '{app="item-api"}'

# Export to file
.\scripts\monitoring\monitoring.ps1 -Mode errors -OutFile .\tmp\loki-errors.json
```

## Notes
- Keep secrets in environment variables only.
- Do not commit real tokens.
