# Refonte du mapping des retours batch Sudoc

Date: 2026-06-22
Depot: `item-api`
Ticket de rattachement: `SOA-692`

## Objectif

Refondre le mapping des erreurs CBS/Sudoc dans `item-api` pour les seuls retours batch et fichiers resultat.

Le besoin ne couvre pas:
- les endpoints API
- les ecrans de simulation
- la bibliotheque `accescbs`

## Constat actuel

Le batch propage encore largement des messages techniques ou generiques directement issus des exceptions:
- `e.getMessage()` est ecrit tel quel dans plusieurs cas
- certains cas sont differencies localement dans `LignesFichierProcessor`
- au moins un cas d'exemplarisation ecrit un message vide dans `retourSudoc`

Cela rend le comportement difficile a maintenir et non homogene entre types de demandes.

## Decision de conception

Creer un composant dedie de mapping, cote `item-api`, pour centraliser la transformation:
- entree: contexte batch + exception
- sortie: message a ecrire dans `retourSudoc`

Le mapping sera pilote prioritairement par:
1. le type d'exception
2. le contexte metier du batch

Le texte du message source ne sera utilise qu'en second choix si le type d'exception ne suffit pas.

## Perimetre fonctionnel

Le composant couvre uniquement les retours ecrits dans les traitements batch et exposes dans les fichiers resultat.

Les types de demandes concernes sont:
- `SUPP`
- `EXEMP`
- `RECOUV`
- `MODIF`

Le point d'integration principal reste `LignesFichierProcessor`.

## Regle generale

- Cas metier connu: ecrire un message metier explicite
- Cas non classe: conserver le message brut `e.getMessage()`

Cette regle preserve l'observabilite fonctionnelle sans etendre artificiellement le scope.

## Premiers cas a couvrir

### Suppression par EPN

Cas deja connus dans `SOA-689`, a conserver dans la solution centralisee:
- notice introuvable lors d'une suppression par `EPN`
- `EPN` absent de la notice

Message cible:
- `EPN inexistant ou erroné : traitement impossible`

### Exemplarisation

Cas a corriger:
- `QueryToSudocException` aujourd'hui transformee en message vide

Comportement cible:
- ne plus jamais ecrire de chaine vide dans `retourSudoc`
- utiliser le mapper central

### Autres cas

Les autres erreurs CBS/Sudoc non encore classees restent en fallback:
- message brut `e.getMessage()`

## Architecture cible

Ajouter un composant dedie, par exemple:
- `BatchRetourSudocMapper`

Responsabilites:
- recevoir l'exception et le contexte de traitement
- reconnaitre les cas metier connus
- retourner le message final pour `retourSudoc`

Contexte minimal attendu:
- type de demande
- type de suppression si la demande est `SUPP`
- ligne en cours si utile

Le composant ne doit:
- ni logger lui-meme la logique metier
- ni faire d'acces CBS
- ni modifier les DTO

Il ne fait que decider du message.

## Impacts de code prevus

### Batch

Refactorer `LignesFichierProcessor` pour:
- deleguer le choix du message au mapper
- reduire les `setRetourSudoc(...)` eparpilles
- conserver les cas nominaux de succes inchanges

### Exemplarisation

Refactorer le traitement actuel qui transforme une `QueryToSudocException` en chaine vide pour passer par le mapper central.

## Strategie de tests

Ajouter des tests unitaires du mapper couvrant:
- suppression `EPN` + notice introuvable
- suppression `EPN` + exemplaire absent de la notice
- fallback sur message brut pour cas non connus
- exemplarisation avec `QueryToSudocException` non vide

Conserver ou etendre les tests batch existants pour verifier l'ecriture finale dans `retourSudoc`.

## Hors perimetre

- modification des messages bas niveau dans `accescbs`
- harmonisation des messages REST
- refonte des erreurs de simulation
- normalisation de tous les messages CBS existants en une seule iteration

## Critere de succes

Le correctif est considere termine si:
- les cas metier connus sont centralises dans un mapper unique
- aucun retour batch ne devient vide pour une erreur CBS/Sudoc
- les cas non classes continuent a remonter `e.getMessage()`
- les tests couvrent au minimum suppression `EPN` et exemplarisation
