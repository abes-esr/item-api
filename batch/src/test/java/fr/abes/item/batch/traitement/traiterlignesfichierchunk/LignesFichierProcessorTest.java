package fr.abes.item.batch.traitement.traiterlignesfichierchunk;

import fr.abes.item.batch.traitement.ProxyRetry;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.batch.traitement.model.LigneFichierDtoSupp;
import fr.abes.item.batch.traitement.retoursudoc.BatchRetourSudocMapper;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.dto.ExemplaireWithTypeDto;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.service.impl.LigneFichierSuppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LignesFichierProcessorTest {
    private final BatchRetourSudocMapper batchRetourSudocMapper = new BatchRetourSudocMapper();

    @Mock
    private StrategyFactory strategyFactory;
    @Mock
    private ProxyRetry proxyRetry;
    @Mock
    private ReferenceService referenceService;
    @Mock
    private IDemandeService demandeService;
    @Mock
    private LigneFichierSuppService ligneFichierSuppService;

    @Test
    void processSuppReturnsExplicitMessageWhenEpnIsNotFoundInNotice() throws Exception {
        LignesFichierProcessor processor = new LignesFichierProcessor(strategyFactory, proxyRetry, referenceService, batchRetourSudocMapper);
        DemandeSupp demandeSupp = new DemandeSupp(123);
        demandeSupp.setTypeSuppression(TYPE_SUPPRESSION.EPN);
        demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ENCOURS));

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setPpn("123456789");
        ligne.setEpn("987654321");

        ReflectionTestUtils.setField(processor, "demandeService", demandeService);
        ReflectionTestUtils.setField(processor, "demandeId", 123);
        ReflectionTestUtils.setField(processor, "demande", demandeSupp);

        when(demandeService.findById(123)).thenReturn(demandeSupp);
        when(strategyFactory.getStrategy(eq(ILigneFichierService.class), eq(TYPE_DEMANDE.SUPP)))
                .thenReturn(ligneFichierSuppService);
        when(ligneFichierSuppService.getExemplairesAndTypeDoc("123456789"))
                .thenReturn(new ExemplaireWithTypeDto());

        LigneFichierDto result = processor.process(ligne);

        assertEquals(Constant.ERR_FILE_EPN_INEXISTANT_OR_ERRONE, result.getRetourSudoc());
        verify(proxyRetry, never()).deleteExemplaire(demandeSupp, ligne);
    }

    @Test
    void processSuppUsesMapperWhenNoticeLookupThrowsQueryToSudocException() throws Exception {
        LignesFichierProcessor processor = new LignesFichierProcessor(strategyFactory, proxyRetry, referenceService, batchRetourSudocMapper);
        DemandeSupp demandeSupp = new DemandeSupp(123);
        demandeSupp.setTypeSuppression(TYPE_SUPPRESSION.EPN);
        demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ENCOURS));

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setPpn("123456789");
        ligne.setEpn("987654321");

        ReflectionTestUtils.setField(processor, "demandeService", demandeService);
        ReflectionTestUtils.setField(processor, "demandeId", 123);
        ReflectionTestUtils.setField(processor, "demande", demandeSupp);

        when(demandeService.findById(123)).thenReturn(demandeSupp);
        when(strategyFactory.getStrategy(eq(ILigneFichierService.class), eq(TYPE_DEMANDE.SUPP)))
                .thenReturn(ligneFichierSuppService);
        when(ligneFichierSuppService.getExemplairesAndTypeDoc("123456789"))
                .thenThrow(new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND));

        LigneFichierDto result = processor.process(ligne);

        assertEquals(Constant.ERR_FILE_EPN_INEXISTANT_OR_ERRONE, result.getRetourSudoc());
        verify(proxyRetry, never()).deleteExemplaire(demandeSupp, ligne);
    }

    @Test
    void processExempFallsBackToMapperWhenProxyRetryThrowsQueryToSudocException() throws Exception {
        LignesFichierProcessor processor = new LignesFichierProcessor(strategyFactory, proxyRetry, referenceService, batchRetourSudocMapper);
        DemandeExemp demandeExemp = new DemandeExemp(456);
        LigneFichierDtoExemp ligne = new LigneFichierDtoExemp();

        ReflectionTestUtils.setField(processor, "demande", demandeExemp);

        org.mockito.Mockito.doThrow(new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND))
                .when(proxyRetry).newExemplaire(demandeExemp, ligne);

        LigneFichierDto result = processor.process(ligne);

        assertEquals(Constant.ERR_FILE_NOTICE_NOT_FOUND, result.getRetourSudoc());
    }
}
