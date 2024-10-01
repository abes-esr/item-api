# dans la cron tab :
# 30 1 1 * * /home/batch/Kopya/current/bin/itemBatchExportStatistiques.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "exportStatistiques") = 0 ]];
then
    java -jar -XX:MaxRAMPercentage=80 item-batch.jar --spring.batch.job.name=exportStatistiques
fi
