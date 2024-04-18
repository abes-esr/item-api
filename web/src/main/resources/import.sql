insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(1, 'En préparation');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(2, 'Préparée');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(3, 'A compléter');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(4, 'En simulation');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(5, 'En attente');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(6, 'En cours de traitement');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(7, 'Terminé');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(8, 'En erreur');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(9, 'Archivé');
insert into ETAT_DEMANDE(NUM_ETAT, LIBELLE) values(10, 'Supprimé');

insert into INDEX_RECHERCHE(NUM_INDEX_RECHERCHE, LIBELLE, CODE, INDEX_ZONES) values (1, 'ISBN', 'ISBN', 1);
insert into INDEX_RECHERCHE(NUM_INDEX_RECHERCHE, LIBELLE, CODE, INDEX_ZONES) values (2, 'ISSN', 'ISSN', 1);
insert into INDEX_RECHERCHE(NUM_INDEX_RECHERCHE, LIBELLE, CODE, INDEX_ZONES) values (3, 'PPN', 'PPN', 1);
insert into INDEX_RECHERCHE(NUM_INDEX_RECHERCHE, LIBELLE, CODE, INDEX_ZONES) values (4, 'Numéro Source', 'SOU', 1);
insert into INDEX_RECHERCHE(NUM_INDEX_RECHERCHE, LIBELLE, CODE, INDEX_ZONES) values (5, 'Date;Auteur;Titre', 'DAT', 3);

insert into ROLE(NUM_ROLE, LIBELLE, USER_GROUP) values(1, 'Admin', 'ABES');
insert into ROLE(NUM_ROLE, LIBELLE, USER_GROUP) values(2, 'Utilisateur', 'coordinateur');

insert into TRAITEMENT(NUM_TRAITEMENT, LIBELLE, NOM_METHODE) values(1, 'Créer une nouvelle zone', 'creerNouvelleZone');
insert into TRAITEMENT(NUM_TRAITEMENT, LIBELLE, NOM_METHODE) values(2, 'Créer une sous-zone', 'ajoutSousZone');
insert into TRAITEMENT(NUM_TRAITEMENT, LIBELLE, NOM_METHODE) values(3, 'Remplacer une sous-zone', 'remplacerSousZone');
insert into TRAITEMENT(NUM_TRAITEMENT, LIBELLE, NOM_METHODE) values(4, 'Supprimer une sous-zone', 'supprimerSousZone');
insert into TRAITEMENT(NUM_TRAITEMENT, LIBELLE, NOM_METHODE) values(5, 'Supprimer une zone', 'supprimerZone');

insert into TYPE_EXEMP(NUM_TYPE_EXEMP, LIBELLE) values(1, 'Monographies électroniques');
insert into TYPE_EXEMP(NUM_TYPE_EXEMP, LIBELLE) values(2, 'Périodiques électroniques');
insert into TYPE_EXEMP(NUM_TYPE_EXEMP, LIBELLE) values(3, 'Autres ressources');

insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(1, 1);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(1, 3);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(2, 2);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(3, 1);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(3, 2);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(3, 3);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(4, 1);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(4, 2);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(4, 3);
insert into INDEX_RECHERCHE_TYPE_EXEMP(NUM_INDEX_RECHERCHE, NUM_TYPE_EXEMP) values(5, 3);

insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(1, '917', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(2, '930', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(3, '991', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(5, 'E316', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(6, 'E317', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(7, 'E319', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(8, 'E856', '4#');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(9, 'L035', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(10, '915', '##');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(11, '955', '41');
insert into ZONES_AUTORISEES(NUM_ZONE, LABEL_ZONE, INDICATEURS) values(12, '920', '##');

insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 1);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 2);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 3);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 5);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 6);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 7);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 8);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 9);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(1, 10);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 1);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 2);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 3);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 5);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 6);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 7);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 8);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 9);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(2, 11);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 1);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 2);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 3);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 5);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 6);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 7);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 9);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 10);
insert into ZONES_AUTORISEES_TYPE_EXEMP(ZONESTYPESEXEMP_NUM_TYPE_EXEMP, ZONESAUTORISEES_NUM_ZONE) values(3, 12);

Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (1,'$a',1,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (2,'$c',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (3,'$d',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (4,'$e',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (5,'$a',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (6,'$i',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (7,'$j',2,true);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (8,'$v',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (9,'$2',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (10,'$a',3,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (18,'$a',5,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (19,'$a',6,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (20,'$a',7,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (21,'$b',7,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (22,'$c',7,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (23,'$d',7,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (24,'$x',7,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (25,'$l',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (26,'$z',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (27,'$q',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (28,'$u',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (29,'$9',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (30,'$a',9,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (31,'$a',10,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (32,'$b',10,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (42,'$a',11,true);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (43,'$k',11,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (44,'$4',11,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (45,'$a',12,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (46,'$b',12,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (47,'$c',12,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (48,'$f',10,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (49,'$b',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (50,'$y',8,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (51,'$l',2,false);
Insert into SOUS_ZONES_AUTORISEES (NUM_SOUS_ZONE,LIBELLE,NUM_ZONE,MANDATORY) values (52,'$k',2,false);