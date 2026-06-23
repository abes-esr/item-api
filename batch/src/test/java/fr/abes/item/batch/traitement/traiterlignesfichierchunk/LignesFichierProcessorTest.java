package fr.abes.item.batch.traitement.traiterlignesfichierchunk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @Mock
    private BatchRetourSudocMapper mockedBatchRetourSudocMapper;

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

    @Test
    void processSuppWithoutEpnUsesMapperWarningMessage() throws Exception {
        LignesFichierProcessor processor = new LignesFichierProcessor(strategyFactory, proxyRetry, referenceService, mockedBatchRetourSudocMapper);
        DemandeSupp demandeSupp = new DemandeSupp(123);
        demandeSupp.setTypeSuppression(TYPE_SUPPRESSION.EPN);
        demandeSupp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ENCOURS));

        LigneFichierDtoSupp ligne = new LigneFichierDtoSupp();
        ligne.setPpn("123456789");

        ReflectionTestUtils.setField(processor, "demandeService", demandeService);
        ReflectionTestUtils.setField(processor, "demandeId", 123);
        ReflectionTestUtils.setField(processor, "demande", demandeSupp);

        when(demandeService.findById(123)).thenReturn(demandeSupp);
        when(strategyFactory.getStrategy(eq(ILigneFichierService.class), eq(TYPE_DEMANDE.SUPP)))
                .thenReturn(ligneFichierSuppService);
        when(ligneFichierSuppService.getExemplairesAndTypeDoc("123456789"))
                .thenReturn(new ExemplaireWithTypeDto());
        when(mockedBatchRetourSudocMapper.mapMissingSuppressionEpn(demandeSupp, ligne))
                .thenReturn(Constant.WARN_NOTICE_EPN_INEXISTANT);

        LigneFichierDto result = processor.process(ligne);

        assertEquals(Constant.WARN_NOTICE_EPN_INEXISTANT, result.getRetourSudoc());
        verify(mockedBatchRetourSudocMapper).mapMissingSuppressionEpn(demandeSupp, ligne);
    }

    @Test
    void processExempLogsThrowableForSudocExceptionPath() throws Exception {
        LignesFichierProcessor processor = new LignesFichierProcessor(strategyFactory, proxyRetry, referenceService, batchRetourSudocMapper);
        DemandeExemp demandeExemp = new DemandeExemp(456);
        LigneFichierDtoExemp ligne = new LigneFichierDtoExemp();

        ReflectionTestUtils.setField(processor, "demande", demandeExemp);

        TestLogAppender appender = attachLogAppender();
        try {
            org.mockito.Mockito.doThrow(new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND))
                    .when(proxyRetry).newExemplaire(demandeExemp, ligne);

            processor.process(ligne);

            LogEvent event = getLastErrorEvent(appender.events);
            assertEquals(Constant.ERROR_FROM_SUDOC_REQUEST_OR_METHOD_SAVEXEMPLAIRE, event.getMessage().getFormattedMessage());
            assertNotNull(event.getThrown());
        } finally {
            detachLogAppender(appender);
        }
    }

    @Test
    void processExempLogsThrowableForGenericExceptionPath() throws Exception {
        LignesFichierProcessor processor = new LignesFichierProcessor(strategyFactory, proxyRetry, referenceService, batchRetourSudocMapper);
        DemandeExemp demandeExemp = new DemandeExemp(456);
        LigneFichierDtoExemp ligne = new LigneFichierDtoExemp();

        ReflectionTestUtils.setField(processor, "demande", demandeExemp);

        TestLogAppender appender = attachLogAppender();
        try {
            org.mockito.Mockito.doThrow(new IllegalStateException("boom"))
                    .when(proxyRetry).newExemplaire(demandeExemp, ligne);

            processor.process(ligne);

            LogEvent event = getLastErrorEvent(appender.events);
            assertEquals(Constant.ERROR_FROM_RECUP_NOTICETRAITEE, event.getMessage().getFormattedMessage());
            assertNotNull(event.getThrown());
        } finally {
            detachLogAppender(appender);
        }
    }

    private TestLogAppender attachLogAppender() {
        TestLogAppender appender = new TestLogAppender();
        appender.start();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        LoggerConfig loggerConfig = context.getConfiguration().getLoggerConfig(LignesFichierProcessor.class.getName());
        loggerConfig.addAppender(appender, null, null);
        context.updateLoggers();
        return appender;
    }

    private void detachLogAppender(TestLogAppender appender) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        LoggerConfig loggerConfig = context.getConfiguration().getLoggerConfig(LignesFichierProcessor.class.getName());
        loggerConfig.removeAppender(appender.getName());
        context.updateLoggers();
        appender.stop();
    }

    private LogEvent getLastErrorEvent(List<LogEvent> events) {
        for (int i = events.size() - 1; i >= 0; i--) {
            LogEvent event = events.get(i);
            if (event.getLevel().isMoreSpecificThan(org.apache.logging.log4j.Level.ERROR)) {
                return event;
            }
        }
        throw new AssertionError("No ERROR log event captured");
    }

    private static final class TestLogAppender extends AbstractAppender {
        private final List<LogEvent> events = new java.util.ArrayList<>();

        private TestLogAppender() {
            super("test-log-appender", null, PatternLayout.createDefaultLayout(), false, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
