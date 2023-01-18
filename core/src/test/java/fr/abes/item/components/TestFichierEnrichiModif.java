package fr.abes.item.components;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.Traitement;
import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.FileCheckingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test pour FichierEnrichi")
class TestFichierEnrichiModif {

	@DisplayName("checkIsSubfieldValid1")
	@Test
	void checkIsSubfieldValid1() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("nokSubfield1.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));

		assertThat(assertThrows(FileCheckingException.class, () -> {
			fic.checkFileContent(demandeModif);
		}).getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide"))
				.isTrue();
	}

	@DisplayName("checkIsSubfieldValid2")
	@Test
	void checkIsSubfieldValid2() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("nokSubfield2.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> {
			fic.checkFileContent(demandeModif);
		}).getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide")).isTrue();
	}

	@DisplayName("checkIsSubfieldValid3")
	@Test
	void checkIsSubfieldValid3() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("nokSubfield3.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> {
			fic.checkFileContent(demandeModif);
		}).getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide")).isTrue();
	}

	@DisplayName("checkIsSubfieldValid4")
	@Test
	void checkIsSubfieldValid4() throws IOException, FileCheckingException {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("okSubfield1.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		fic.checkFileContent(demandeModif);
	}

	@DisplayName("checkIsSubfieldValid5")
	@Test
	void checkIsSubfieldValid5() throws IOException, FileCheckingException {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("okSubfield2.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		fic.checkFileContent(demandeModif);
	}

	@DisplayName("checkIsSubfieldValid6")
	@Test
	void checkIsSubfieldValid6() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("okSubfield3.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> {
			fic.checkFileContent(demandeModif);
		}).getMessage().contains(Constant.ERR_FILE_WRONGRCR))
				.isTrue();
	}

	@DisplayName("checkColMissing")
	@Test
	void checkColMissing() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("colMissing.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> {
			fic.checkFileContent(demandeModif);
		}).getMessage().contains(Constant.ERR_FILE_3COL))
				.isTrue();
	}

	@DisplayName("checkFileBodyLineLength")
	@Test
	void checkFileBodyLineLength() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("nokBodyFileLineLength.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> {
			fic.checkFileContent(demandeModif);
		}).getMessage().contains("Il y a un problème lié à la longueur de la ligne.")).isTrue();
	}

	@DisplayName("checkRcr")
	@Test
	void checkRcrNOk() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("nokBodyRcr.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains(Constant.ERR_FILE_WRONGRCR)).isTrue();
	}

	@DisplayName("creerNouvelleZoneNOk")
	@Test
	void creerNouvelleZoneNOk() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("nokcreernouvellezone.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("La valeur de la 4è colonne ne doit pas être vide")).isTrue();
	}

	@DisplayName("fourthColWithZoneENOk")
	@Test
	void fourthColWithZoneENOk() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokCol4ZoneE.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide")).isTrue();
	}

	@DisplayName("fourthColWithZone930b")
	@Test
	void fourthColWithZone930b() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokCol4Zone930b.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("impossible de lancer un traitement sur la zone")).isTrue();
	}

	@DisplayName("fourthColWithZone955")
	@Test
	void fourthColWithZone955() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokCol4Zone955.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("impossible de lancer un traitement sur la zone")).isTrue();
	}

	@DisplayName("fourthColWithZoneAXX")
	@Test
	void fourthColWithZoneAXX() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokCol4ZoneAXX.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide")).isTrue();
	}

	@DisplayName("fourthColWithZoneexx")
	@Test
	void fourthColWithZoneexx() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokCol4Zonee01.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide")).isTrue();
	}

	@DisplayName("supprimerZoneNOk")
	@Test
	void supprimerZoneNOk() throws IOException, FileCheckingException {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("noksupprimerzone.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		demandeModif.setTraitement(new Traitement(2, "Supprimer une zone", "supprimerZone"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("La valeur de la 4è colonne doit être vide")).isTrue();
	}

	@DisplayName("fileOK")
	@Test
	void fileOK() throws IOException, FileCheckingException {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("okWithData.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		fic.checkFileContent(demandeModif);
	}

	@DisplayName("checkTraitement")
	@Test
	void checkTraitement() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("okWithData.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		demandeModif.setTraitement(null);
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains(Constant.ERR_FILE_NOTRAIT)).isTrue();
	}

	@DisplayName("checkPpn")
	@Test
	void checkPpn() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokPpn.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains(Constant.ERR_FILE_WRONGPPN)).isTrue();
	}

	@DisplayName("checkEpn")
	@Test
	void checkEpn() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(1, "Ajout une sous-zone", "ajoutSousZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokEpn.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("La valeur de l'epn n'est pas conforme")).isTrue();

	}

	@DisplayName("checkE856")
	@Test
	void checkE856() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(3, "Créer une nouvelle zone", "creerNouvelleZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("NokE856.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("impossible de lancer un traitement sur la zone")).isTrue();
	}

	@DisplayName("checke01")
	@Test
	void checke01() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(3, "Créer une nouvelle zone", "creerNouvelleZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("Nokexx.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("impossible de lancer un traitement sur la zone")).isTrue();
	}

	@DisplayName("checkDollar")
	@Test
	void testDollar() {
		DemandeModif demandeModif = new DemandeModif("341720001", new Date(), new Date(), "", "", "", new EtatDemande(1), new Utilisateur(1), new Traitement(3, "Créer une nouvelle zone", "creerNouvelleZone"));
		FichierEnrichiModif fic = new FichierEnrichiModif("dollarforbid.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif)).getMessage().contains("le caractère $ est interdit dans la 4è colonne")).isTrue();
	}

	@Test
	void check4thColumn() {
		DemandeModif demandeModif = new DemandeModif(
				35678, /*numDemande*/
				"340322102", /*Rcr*/
				new Date(2019, Calendar.APRIL, 1), /*DateCreation*/
				new Date(2019, Calendar.APRIL, 15), /*DateModification*/
				"931",
				"$a",
				"ceci est un commentaire",
				new EtatDemande(3, "a completer"),
				new Utilisateur(35673, "item@abes.fr", "45"),
				new Traitement(1, "Créer une nouvelle zone", "creerNouvelleZone"),
				"45");
		FichierEnrichiModif fic = new FichierEnrichiModif("930$c-fichier_demande.csv");
		fic.setPath(Paths.get("src/test/resources/fichierEnrichiModif"));
		//Actual
		assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeModif))
				.getMessage().contains("La valeur en-tête de la quatrieme colonne n'est pas valide") ;

	}
}
