# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierRecouv.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.names=archiverDemandesPlusDeTroisMois") < 1 ]];
then
    java -jar -XX:MaxRAMPercentage=95 /scripts/item-batch.jar --spring.batch.job.names=archiverDemandesPlusDeTroisMois
fi