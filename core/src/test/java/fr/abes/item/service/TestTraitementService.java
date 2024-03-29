package fr.abes.item.service;

import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.item.service.impl.TraitementService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test Couche service / traitement")
public class TestTraitementService {
    private static TraitementService tManager;

    private static Properties prop;

    @BeforeAll
    static void initAll() {
        tManager = new TraitementService();
    }


    @DisplayName("test creerNouvelleZone")
    @Test
    void creerNouvelleZone() throws ZoneException {
        StringBuilder noticeInit = new StringBuilder().append("e01 $a17-09-18$bx\r" +
                "930 ##$b341720001\r" +
                "A97 17-09-18 10:51:56.000\r" +
                "A98 341720001:17-09-18\r" +
                "A99 618828249\r").append(Constants.STR_0D + Constants.STR_0D);
        Exemplaire result = tManager.creerNouvelleZone(noticeInit.toString(), "915", "$a", "1");
        assertThat(result.toString()).startsWith("e01 $a17-09-18$bx\r" +
                "915 $a1\r" +
                "930 ##$b341720001\r" +
                "991 ##$a");
        assertThat(result.toString()).endsWith("A97 17-09-18 10:51:56.000\r" +
                "A98 341720001:17-09-18\r" +
                "A99 618828249\r");
    }

    @DisplayName("test creerNouvelleZoneWithRepetableTag")
    @Test
    void creerNouvelleZoneWithRepeatableTag() throws ZoneException {
        StringBuilder noticeInit = new StringBuilder().append("e01 $a17-09-18$bx\r" +
                "930 ##$b341720001\r" +
                "A97 17-09-18 10:51:56.000\r" +
                "A98 341720001:17-09-18\r" +
                "A99 618828249\r").append(Constants.STR_0D + Constants.STR_0D + Constants.STR_1E);
        Exemplaire result = tManager.creerNouvelleZone(noticeInit.toString(), "930", "$c", "test");
        assertThat(result.toString()).startsWith("e01 $a17-09-18$bx\r" +
                "930 ##$b341720001\r" +
                "930 $ctest\r" +
                "991 ##$a");
        assertThat(result.toString()).endsWith("A97 17-09-18 10:51:56.000\r" +
                "A98 341720001:17-09-18\r" +
                "A99 618828249\r");
    }


    @DisplayName("test supprimerZone")
    @Test
    public void supprimerZone() throws ZoneException {
        StringBuilder notice = new StringBuilder().append(
                "e02 $a02-12-05$bx\r" +
                "930 ##$b340322102$a791 ETH$ju\r" +
                "991 $aExemplaire créé automatiquement par l'ABES\r" +
                "A97 02-12-05 10:01:06.000\r" +
                "A98 340322102:02-12-05\r" +
                "A99 248333186" + Constants.STR_0D);
        Exemplaire result = tManager.supprimerZone(notice.toString(), "991");
        assertThat(result.toString()).startsWith(
                "e02 $a02-12-05$bx\r" +
                "930 ##$b340322102$a791 ETH$ju\r" +
                "991 ##$a");
        assertThat(result.toString()).endsWith(
                "A97 02-12-05 10:01:06.000\r" +
                        "A98 340322102:02-12-05\r" +
                        "A99 248333186\r"
        );
    }

    @DisplayName("Test ajout 991")
    @Test
    public void ajout991() throws ZoneException {
        StringBuilder notice = new StringBuilder().append(
                "e02 $a02-12-05$bx\r" +
                "930 ##$b340322102$a791 ETH$ju\r" +
                "991 ##$aExemplaire créé automatiquement par l'ABES\r" +
                "A97 02-12-05 10:01:06.000\r" +
                "A98 340322102:02-12-05\r" +
                "A99 248333186" + Constants.STR_0D);
        Exemplaire exemp = new Exemplaire(notice.toString());
        String newExemp = tManager.ajout991(exemp).toString();
        assertThat(newExemp).containsOnlyOnce("991 ##$aExemplaire modifié automatiquement");
        assertThat(newExemp).containsOnlyOnce("991 ##$aExemplaire créé automatiquement par l'ABES");
    }

}
