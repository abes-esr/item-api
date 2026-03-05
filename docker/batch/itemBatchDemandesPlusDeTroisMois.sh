#!/bin/bash

# dans la cron tab :
# 0 20 * * 0 /scripts/itemBatchDemandesPlusDeTroisMois.sh > /dev/null 2>&1

set -euo pipefail

LANG=fr_FR.UTF-8

# Evite un chevauchement si une execution precedente est encore en cours.
if [[ $(pgrep -cf "itemBatchDemandesPlusDeTroisMois.sh") -gt 1 ]]; then
  echo "Une execution de itemBatchDemandesPlusDeTroisMois est deja en cours, sortie."
  exit 0
fi

run_job() {
  local job_name="$1"
  echo "Lancement du job batch: ${job_name}"
  java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.launch.JarLauncher --spring.batch.job.name="${job_name}"
}

# Ordre conserve par rapport a l'ancien scheduling.
run_job "archiverDemandesPlusDeTroisMois"
run_job "statutSupprimeDemandesPlusDeTroisMois"
run_job "suppressionDemandesPlusDeTroisMois"

echo "Traitement demandes > 3 mois termine."
