###
# Image pour la compilation
FROM maven:3-eclipse-temurin-17 as build-image
WORKDIR /build/
# Installation et configuration de la locale FR
RUN set -xeu && \
    export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates `# stays, not having this is just not useful` \
        curl \
        && \
    mkdir /graalvm && \
    curl -fsSL "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-17.0.9/graalvm-community-jdk-17.0.9_linux-x64_bin.tar.gz" \
        | tar -zxC /graalvm --strip-components 1 && \
    find /graalvm -name "*src.zip"  -printf "Deleting %p\n" -exec rm {} + && \
    { test ! -d /graalvm/legal || tar czf /graalvm/legal.tgz /graalvm/legal/; } && \
    { test ! -d /graalvm/legal || rm -r /graalvm/legal; } && \
    rm -rf /graalvm/man `# does not exist in java11 package` && \
    echo Cleaning up... && \
    apt-get remove -y \
        curl \
        && \
    apt-get autoremove -y && \
    apt-get clean && rm -r "/var/lib/apt/lists"/* && \
    # PATH is set via ENV below. However, `bash -l` will source `/etc/profile` and set $PATH on its own.
    echo 'PATH="/graalvm/bin:$PATH"' | install --mode 0644 /dev/stdin /etc/profile.d/graal-on-path.sh && \
    echo OK

ENV PATH=/graalvm/bin:$PATH JAVA_HOME=/graalvm

# On lance la compilation Java
# On débute par une mise en cache docker des dépendances Java
# cf https://www.baeldung.com/ops/docker-cache-maven-dependencies
COPY ./pom.xml /build/pom.xml
COPY ./core/pom.xml /build/core/pom.xml
COPY ./web/pom.xml /build/web/pom.xml
COPY ./batch/pom.xml /build/batch/pom.xml
RUN mvn verify --fail-never
# et la compilation du code Java
COPY ./core/   /build/core/
COPY ./web/    /build/web/
COPY ./batch/  /build/batch/
RUN mvn --batch-mode \
        -Pnative \
        -Dmaven.test.skip=false \
        -Duser.timezone=Europe/Paris \
        -Duser.language=fr \
        package


###
# Image pour le module API
#FROM tomcat:9-jdk11 as api-image
#COPY --from=build-image /build/web/target/*.war /usr/local/tomcat/webapps/ROOT.war
#CMD [ "catalina.sh", "run" ]
FROM tomcat:9-jdk17 as api-image
WORKDIR /app/
COPY --from=build-image /build/web/target/*.jar /app/item.jar
ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["java","-jar","/app/item.jar"]


###
# Image pour le module batch
# Remarque: l'image openjdk:11 n'est pas utilisée car nous avons besoin de cronie
#           qui n'est que disponible sous centos/rockylinux.
FROM rockylinux:8 as batch-image
WORKDIR /scripts/
#locales fr
# Les locales fr_FR
RUN dnf install langpacks-fr glibc-all-langpacks -y
ENV LANG fr_FR.UTF-8
ENV LANGUAGE fr_FR:fr
ENV LC_ALL fr_FR.UTF-8

# Configuration du fuseau horaire
# Pour résoudre le problème de décalage horaire dans les logs
# de votre conteneur item-batch, vous devez ajouter ces instructions dans la partie batch-image de votre Dockerfile.
ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# systeme pour les crontab
# cronie: remplacant de crond qui support le CTRL+C dans docker (sans ce système c'est compliqué de stopper le conteneur)
# gettext: pour avoir envsubst qui permet de gérer le template tasks.tmpl
RUN yum install -y procps
RUN dnf install -y cronie gettext && \
    crond -V && rm -rf /etc/cron.*/*
COPY ./docker/batch/tasks.tmpl /etc/cron.d/tasks.tmpl
# Le JAR et le script pour le batch de LN
RUN dnf install -y java-17-openjdk

RUN dnf install -y tzdata && \
    ln -fs /usr/share/zoneinfo/Europe/Paris /etc/localtime && \
    echo "Europe/London" > /etc/timezone

COPY ./docker/batch/itemBatchArchiverDemandesPlusDeTroisMois.sh /scripts/itemBatchArchiverDemandesPlusDeTroisMois.sh
RUN chmod +x /scripts/itemBatchArchiverDemandesPlusDeTroisMois.sh
COPY ./docker/batch/itemBatchExportStatistiques.sh /scripts/itemBatchExportStatistiques.sh
RUN chmod +x /scripts/itemBatchExportStatistiques.sh
COPY ./docker/batch/itemBatchStatutSupprimeDemandesPlusDeTroisMois.sh /scripts/itemBatchStatutSupprimeDemandesPlusDeTroisMois.sh
RUN chmod +x /scripts/itemBatchStatutSupprimeDemandesPlusDeTroisMois.sh
COPY ./docker/batch/itemBatchSuppressionDemandesPlusDeTroisMois.sh /scripts/itemBatchSuppressionDemandesPlusDeTroisMois.sh
RUN chmod +x /scripts/itemBatchSuppressionDemandesPlusDeTroisMois.sh
COPY ./docker/batch/itemBatchTraiterLigneFichierExemp.sh /scripts/itemBatchTraiterLigneFichierExemp.sh
RUN chmod +x /scripts/itemBatchTraiterLigneFichierExemp.sh
COPY ./docker/batch/itemBatchTraiterLigneFichierModif.sh /scripts/itemBatchTraiterLigneFichierModif.sh
RUN chmod +x /scripts/itemBatchTraiterLigneFichierModif.sh
COPY ./docker/batch/itemBatchTraiterLigneFichierRecouv.sh /scripts/itemBatchTraiterLigneFichierRecouv.sh
RUN chmod +x /scripts/itemBatchTraiterLigneFichierRecouv.sh
COPY ./docker/batch/itemBatchTraiterLigneFichierSupp.sh /scripts/itemBatchTraiterLigneFichierSupp.sh
RUN chmod +x /scripts/itemBatchTraiterLigneFichierSupp.sh

COPY --from=build-image /build/batch/target/* /scripts/
RUN chmod +x /scripts/item-batch.jar

COPY ./docker/batch/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["crond", "-n"]
