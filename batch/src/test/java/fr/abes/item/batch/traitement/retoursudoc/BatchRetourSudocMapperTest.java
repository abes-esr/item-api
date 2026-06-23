package fr.abes.item.batch.traitement.retoursudoc;

import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.batch.traitement.model.LigneFichierDtoRecouv;
import fr.abes.item.batch.traitement.model.LigneFichierDtoSupp;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.exception.QueryToSudocException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchRetourSudocMapperTest {

    private final BatchRetourSudocMapper mapper = new BatchRetourSudocMapper();

    @Test
    void mapsSuppressionEpnNoticeNotFoundToExplicitBusinessMessage() {
        DemandeSupp demande = new DemandeSupp(1);
        demande.setTypeSuppression(TYPE_SUPPRESSION.EPN);

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setEpn("987654321");

        String result = mapper.map(
                new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND),
                demande,
                ligne
        );

        assertEquals(Constant.ERR_FILE_EPN_INEXISTANT_OR_ERRONE, result);
    }

    @Test
    void keepsRawMessageForUnknownQueryToSudocCases() {
        DemandeExemp demande = new DemandeExemp(1);
        LigneFichierDtoExemp ligne = new LigneFichierDtoExemp();

        String result = mapper.map(
                new QueryToSudocException(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + "230721486,23309668X"),
                demande,
                ligne
        );

        assertEquals(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + "230721486,23309668X", result);
    }

    @Test
    void keepsRawMessageForGenericIOExceptionFallback() {
        DemandeExemp demande = new DemandeExemp(1);
        LigneFichierDtoExemp ligne = new LigneFichierDtoExemp();

        String result = mapper.map(new IOException("socket timeout"), demande, ligne);

        assertEquals("socket timeout", result);
    }

    @Test
    void mapsSuppressionEpnMissingInNoticeWithoutException() {
        DemandeSupp demande = new DemandeSupp(1);
        demande.setTypeSuppression(TYPE_SUPPRESSION.EPN);

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setEpn("987654321");

        assertEquals(
                Constant.ERR_FILE_EPN_INEXISTANT_OR_ERRONE,
                mapper.mapMissingSuppressionEpn(demande, ligne)
        );
    }

    @Test
    void doesNotMapSuppressionPpnNoticeNotFoundToExplicitEpnMessage() {
        DemandeSupp demande = new DemandeSupp(1);
        demande.setTypeSuppression(TYPE_SUPPRESSION.PPN);

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setEpn("987654321");

        String result = mapper.map(
                new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND),
                demande,
                ligne
        );

        assertEquals(Constant.ERR_FILE_NOTICE_NOT_FOUND, result);
    }

    @Test
    void doesNotMapSuppressionWithoutEpnToExplicitEpnMessage() {
        DemandeSupp demande = new DemandeSupp(1);
        demande.setTypeSuppression(TYPE_SUPPRESSION.EPN);

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();

        String result = mapper.map(
                new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND),
                demande,
                ligne
        );

        assertEquals(Constant.ERR_FILE_NOTICE_NOT_FOUND, result);
    }

    @Test
    void keepsGenericWarningForMissingSuppressionFallbackCase() {
        DemandeSupp demande = new DemandeSupp(1);
        demande.setTypeSuppression(TYPE_SUPPRESSION.PPN);

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setEpn("987654321");

        assertEquals(
                Constant.WARN_NOTICE_EPN_INEXISTANT,
                mapper.mapMissingSuppressionEpn(demande, ligne)
        );
    }

    @Test
    void mapsRecouvInvalidSearchIndexToBusinessMessage() {
        DemandeRecouv demande = new DemandeRecouv(1);
        LigneFichierDtoRecouv ligne = new LigneFichierDtoRecouv();

        String result = mapper.map(
                new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_CODE_NOT_COMPLIANT),
                demande,
                ligne
        );

        assertEquals(Constant.ERR_FILE_SEARCH_INDEX_NOT_COMPLIANT, result);
    }
}
