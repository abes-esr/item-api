# SOA-518 - Resume de migration (JDK 21 + Spring Boot 3.5.x)

Date: 2026-02-16  
Contexte: branche dediee migration.

## Decision de version

- Cible ticket: `Spring Boot 3.5.6` + `JDK 21`.
- Option recommandee: `Spring Boot 3.5.10` (meme branche 3.5, patch plus recent et plus robuste en maintenance/securite), sauf contrainte explicite de conformite stricte a `3.5.6`.

## Progression globale

Etapes terminees:
1. Baseline et verification initiale.
2. Upgrade socle JDK/BOM/plugins.
3. Alignement dependances + migrations techniques associees (JWT, springdoc, httpclient, opencsv).

Etapes restantes:
4. Docker/CI (validation explicite et harmonisation complete Java 21 runtime/build).
5. Validation de non-regression orientee metier (smoke tests cibles).
6. Revue securite finale + durcissement de configuration.

## Modifications effectuees jusqu'ici

### A. Socle deja migre (etapes precedentes)
- Java cible du build en 21.
- Spring Boot aligne en `3.5.10`.
- Migration tests `@MockBean` -> `@MockitoBean`.
- Agent Mockito configure dans Surefire pour stabiliser le comportement avec JDK 21.

### B. Alignement dependances et code (session courante)

#### Parent/gestion de versions
- `pom.xml`
  - ajout:
    - `jjwt.version=0.13.0`
    - `springdoc.version=2.8.15`
    - `httpclient5.version=5.5.2`

#### Module core
- `core/pom.xml`
  - suppression version explicite de `jackson-databind` pour laisser Spring Boot BOM gerer.

#### Module web
- `web/pom.xml`
  - retrait `io.jsonwebtoken:jjwt:0.9.1`
  - ajout:
    - `io.jsonwebtoken:jjwt-api:${jjwt.version}`
    - `io.jsonwebtoken:jjwt-impl:${jjwt.version}` (runtime)
    - `io.jsonwebtoken:jjwt-jackson:${jjwt.version}` (runtime)
  - mise a jour `springdoc-openapi-starter-webmvc-ui` vers `${springdoc.version}`

- `web/src/main/java/fr/abes/item/security/JwtTokenProvider.java`
  - migration API JJWT legacy -> API moderne 0.13
  - signature HS512 via cle `SecretKey` derivee avec `Keys.hmacShaKeyFor(...)`
  - verification/parsing via `parser().verifyWith(...).build().parseSignedClaims(...)`

- `web/src/main/java/fr/abes/item/security/SpringSecurityConfig.java`
  - autorisation des endpoints Springdoc 2.x (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`)

#### Module batch
- `batch/pom.xml`
  - retrait `httpclient` + `httpmime` (4.x)
  - ajout `org.apache.httpcomponents.client5:httpclient5:${httpclient5.version}`
  - retrait des versions explicites BOM-managees (`spring-retry`, `spring-boot-starter-aop`)
  - retrait de la declaration locale `opencsv` legacy

- `batch/src/main/java/fr/abes/item/batch/mail/impl/Mailer.java`
  - migration Apache HttpClient 4 -> 5 (`org.apache.hc.*`)
  - execution HTTP avec fermeture explicite de la reponse (try-with-resources)

- `batch/src/main/java/fr/abes/item/batch/webstats/ExportStatistiquesTasklet.java`
  - adaptation constructeur `CSVWriter` compatible OpenCSV recent

## Validation technique

Commande executee:
- `mvn -s specs/maven-settings-baseline.xml clean test`

Resultat:
- `BUILD SUCCESS` sur `core`, `web`, `batch`.

Note execution:
- Un premier echec etait lie a un shell en Java 17 (`invalid target release: 21`).
- Rerun effectue avec `JAVA_HOME` force sur `C:\Program Files\Java\jdk-21`.

## Risques de casse restants (actualises)

1. JWT secret insuffisant
- Avec JJWT 0.13 + HS512, un secret trop court peut casser la generation/validation de tokens en runtime.
- Action: verifier la longueur et la rotation des secrets selon l'environnement.

2. Ecart d'environnement Java
- Un poste/agent CI encore en Java 17 fera echouer la compilation (target 21).
- Action: forcer Java 21 dans CI et dans la documentation d'execution locale.

3. Logs de test
- Warning Log4j de style ANSI "dark" non bloquant mais bruyant.
- Action: nettoyer le pattern de log test quand on traite le lot hygiene/qualite.

## Prochaine etape recommandee

Etape suivante a lancer:
1. Docker/CI Java 21 de bout en bout.
2. Puis smoke tests metier cibles (auth JWT, endpoints principaux, batch mail/stats).
3. Ensuite revue securite finale (SBOM + scan CVE) avant merge.

## Etape 4 - Docker/CI JDK 21 (en cours)

Changements deja prepares:
- `Dockerfile` migre JDK21 pour build + runtime API + runtime batch.
- Runtime batch base image alignee sur `rockylinux:9` pour compatibilite `java-21-openjdk`.
- Fuseau batch corrige en `Europe/Paris`.
- Nouveau workflow manuel de publication test Docker Hub:
  - `.github/workflows/dockerhub-jdk21-test-publish.yml`
  - input `test_tag`
  - push de deux tags temporaires:
    - `<prefix>:<test_tag>-api`
    - `<prefix>:<test_tag>-batch`

Validation locale:
- Daemon Docker local indisponible pendant le test.
- Validation operationnelle a faire via GitHub Actions (workflow_dispatch) ou serveur avec Docker actif.

## Prochaine action immediate

1. Push branche `soa-518-step4-docker-jdk21`.
2. Lancer le workflow `dockerhub-jdk21-test-publish` avec un tag de test.
3. Deployer ces tags sur serveur distant pour smoke test runtime.
