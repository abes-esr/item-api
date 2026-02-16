# Plan d'optimisation Docker + GitHub Actions (pas à pas)

Objectif global: réduire le temps de build/push des images Docker (API + batch) et garder un pipeline lisible et stable.

Ce document sert de feuille de route commune. On appliquera les étapes dans l'ordre, en mesurant les gains à chaque étape.

## Etape 1 - Optimiser `.dockerignore`

### Pourquoi
Le build Docker envoie un contexte (fichiers du repo) au daemon/buildx. Plus ce contexte est gros, plus le build démarre lentement.

### Ce qu'on va faire
1. Inventorier ce qui ne sert pas à construire l'image.
2. Exclure ces éléments dans `.dockerignore`.
3. Vérifier qu'on n'exclut pas des fichiers nécessaires au build.

### Exclusions typiques
- `.git`
- `**/target`
- `node_modules`
- `*.log`
- docs temporaires
- fichiers locaux de dev (`.idea`, `.vscode`, etc.)
- artefacts de test non nécessaires

### Validation
- Le build passe toujours.
- Le temps de l'étape "Build" diminue (surtout au début du job).

---

## Etape 2 - Réorganiser le `Dockerfile` pour maximiser le cache

### Pourquoi
Le cache Docker repose sur l'ordre des couches. Si on copie tout le code trop tôt, le cache saute à chaque changement.

### Ce qu'on va faire
1. Copier d'abord les descripteurs de dépendances (`pom.xml`, `*/pom.xml`).
2. Télécharger/préchauffer les dépendances Maven (`dependency:go-offline` ou équivalent).
3. Copier ensuite seulement le code source.
4. Compiler/package après la phase dépendances.

### Résultat attendu
Un changement dans le code Java n'invalide plus la couche dépendances, donc build plus rapide.

### Validation
- Build OK.
- Les logs Docker montrent des couches "CACHED" sur les dépendances.

---

## Etape 3 - Ajuster les workflows pour éviter les builds Docker inutiles

### Pourquoi
Aujourd'hui, même des changements sans impact Docker peuvent déclencher un build d'image.

### Ce qu'on va faire
1. Définir des règles `paths`/`paths-ignore` plus fines.
2. Déclencher Docker seulement si fichiers pertinents changent (Dockerfile, modules backend, scripts build).
3. Garder les tests applicatifs indépendants si nécessaire.

### Validation
- Un changement docs seul ne déclenche plus Docker.
- Un changement code backend déclenche bien Docker.

---

## Etape 4 - Mesurer avant/après pour objectiver les gains

### Pourquoi
Sans mesure, on ne sait pas ce qui améliore vraiment.

### Ce qu'on va faire
1. Relever une baseline (durée des jobs/steps actuels).
2. Après chaque étape, relancer sur une branche test.
3. Comparer les durées:
   - setup
   - build image API
   - build image batch
   - push (si applicable)

### Outil
Historique GitHub Actions + tableau simple de suivi dans un fichier local.

### Validation
- Gain visible et reproductible (pas seulement un run isolé).

---

## Etape 5 - Optimiser le partage de couches entre `api-image` et `batch-image`

### Pourquoi
API et batch partagent souvent beaucoup de dépendances/couches. On peut réduire le coût des doubles builds.

### Ce qu'on va faire
1. Revoir les stages communs du Dockerfile multi-stage.
2. Mettre une base commune la plus large possible.
3. Conserver des étapes spécifiques minimales pour API et batch.

### Validation
- Les deux targets buildent toujours.
- Moins de couches reconstruites entre API et batch.

---

## Etape 6 - Options avancées (infra/stratégie)

### 6A - Prébuild artefacts Java hors Docker
- Faire `mvn package` dans un job dédié.
- Injecter l'artefact dans une image runtime légère.
- Avantage: build Docker plus court.
- Attention: stratégie de reproductibilité à cadrer.

### 6B - Runner plus performant
- Self-hosted runner ou machine plus puissante.
- Avantage: réduction directe du temps CPU/IO.
- Attention: coût et maintenance infra.

### Validation
- Décision basée sur rapport gain/coût.

---

## Méthode de travail proposée

1. Une étape à la fois.
2. Commit dédié par étape.
3. Run GitHub Actions après chaque étape.
4. Décision "on garde / on ajuste / on revert".

## Critères de réussite

- Pipeline plus rapide.
- Aucun impact fonctionnel sur la construction des images.
- Workflows plus lisibles et maintenables.
- Aucun écrasement involontaire des images de `develop` pendant les tests de branche.
