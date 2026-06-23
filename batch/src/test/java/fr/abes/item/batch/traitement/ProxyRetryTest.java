package fr.abes.item.batch.traitement;

import fr.abes.cbs.process.ProcessCBS;
import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.IndexRecherche;
import fr.abes.item.core.entities.item.TypeExemp;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.service.impl.LigneFichierExempService;
import fr.abes.item.core.service.impl.LigneFichierModifService;
import fr.abes.item.core.service.impl.LigneFichierRecouvService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.annotation.Retryable;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProxyRetryTest {
    @Mock
    private TraitementService traitementService;
    @Mock
    private StrategyFactory strategyFactory;
    @Mock
    private LigneFichierModifService ligneFichierModifService;
    @Mock
    private LigneFichierExempService ligneFichierExempService;
    @Mock
    private LigneFichierRecouvService ligneFichierRecouvService;
    @Mock
    private ProcessCBS cbs;

    @Test
    void newExemplaireRethrowsQueryToSudocExceptionAfterFillingBatchMetadata() throws Exception {
        ProxyRetry proxyRetry = new ProxyRetry(
                traitementService,
                strategyFactory,
                ligneFichierModifService,
                ligneFichierExempService,
                ligneFichierRecouvService
        );
        DemandeExemp demande = new DemandeExemp(123);
        demande.setIndexRecherche(new IndexRecherche(1, "PPN", "PPN", 1));
        demande.setTypeExemp(new TypeExemp(2));

        LigneFichierDtoExemp ligne = new LigneFichierDtoExemp();
        ligne.setIndexRecherche("123456789");

        QueryToSudocException expected = new QueryToSudocException("notice introuvable");

        when(ligneFichierExempService.getQueryToSudoc(eq("PPN"), eq(2), any(String[].class)))
                .thenReturn("che PPN 123456789");
        when(ligneFichierExempService.launchQueryToSudoc(demande, "123456789"))
                .thenThrow(expected);
        when(ligneFichierExempService.getNbReponses()).thenReturn(2);
        when(traitementService.getCbs()).thenReturn(cbs);
        when(cbs.getListePpn()).thenReturn(new StringBuilder("230721486;23309668X;"));

        QueryToSudocException thrown = assertThrows(
                QueryToSudocException.class,
                () -> proxyRetry.newExemplaire(demande, ligne)
        );

        assertSame(expected, thrown);
        assertEquals(2, ligne.getNbReponses());
        assertEquals("230721486,23309668X,", ligne.getListePpn());
        assertNull(ligne.getRetourSudoc());
    }

    @Test
    void newExemplaireDoesNotRetryQueryToSudocException() throws NoSuchMethodException {
        Method method = ProxyRetry.class.getMethod(
                "newExemplaire",
                DemandeExemp.class,
                LigneFichierDtoExemp.class
        );

        Retryable retryable = method.getAnnotation(Retryable.class);

        assertTrue(Arrays.asList(retryable.noRetryFor()).contains(QueryToSudocException.class));
    }
}
