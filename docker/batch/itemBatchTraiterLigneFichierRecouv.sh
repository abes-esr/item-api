# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierRecouv.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "traiterLigneFichierRecouv") < 1 ]];
then
    java -jar -XX:MaxRAMPercentage=80 item-batch.jar --spring.batch.job.name=traiterLigneFichierRecouv --server.port=8084
fi
