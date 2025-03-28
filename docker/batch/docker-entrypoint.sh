#!/bin/bash

# Paramètres par défaut du conteneur
export ITEM_BATCH_AT_STARTUP=${ITEM_BATCH_AT_STARTUP:='1'}
export ITEM_BATCH_CRON_STAT=${ITEM_BATCH_CRON_STAT:='0 7 1 * *'}

# Réglage de /etc/environment pour que les crontab s'exécutent avec les bonnes variables d'env
echo "$(env)
LANG=en_US.UTF-8" > /etc/environment

# Charge la crontab depuis le template
if [ "$ITEM_BATCH_DEPLOY_CRON" = "1" ]; then
  envsubst < /etc/cron.d/tasks.tmpl > /etc/cron.d/tasks
  echo "-> Installation des crontab :"
  cat /etc/cron.d/tasks
  crontab /etc/cron.d/tasks
fi

# Force le démarrage du batch au démarrage du conteneur
if [ "$ITEM_BATCH_AT_STARTUP" = "1" ]; then
  echo "-> Lancement de itemBatchTraiterLigneFichierExemp.sh au démarrage du conteneur"
  /scripts/itemBatchTraiterLigneFichierExemp.sh
  echo "-> Lancement de itemBatchTraiterLigneFichierModif.sh au démarrage du conteneur"
  /scripts/itemBatchTraiterLigneFichierModif.sh
  echo "-> Lancement de itemBatchTraiterLigneFichierRecouv.sh au démarrage du conteneur"
  /scripts/itemBatchTraiterLigneFichierRecouv.sh
  echo "-> Lancement de itemBatchTraiterLigneFichierSupp.sh au démarrage du conteneur"
  /scripts/itemBatchTraiterLigneFichierSupp.sh
fi

exec "$@"
