package fr.abes.item.batch.traitement;

import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.service.IDemandeService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

@Slf4j
public class DeleteStatusDemandesExempTasklet implements Tasklet, StepExecutionListener {
    private final StrategyFactory strategyFactory;
    List<DemandeExemp> demandes;

    public DeleteStatusDemandesExempTasklet(StrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        log.info("Passage en statut supprimé (mais conservation en base) des demandes d'exemplarisation, modification et recouvrement lancé");
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        log.info(Constant.ENTER_EXECUTE_FROM_GETNEXTDEMANDEEXEMPTODELETEDSTATUSTASKLET);
        try {
            IDemandeService service = strategyFactory.getStrategy(IDemandeService.class, TYPE_DEMANDE.EXEMP);
            this.demandes = (List<DemandeExemp>) service.getIdNextDemandeToPlaceInDeletedStatus();
            if (this.demandes == null) {
                log.warn(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            //Iteration sur chaque demande pour en modifier le statut
            for (Demande demande : this.demandes) {
                log.info("Passage de la demande d'exemplarisation " + demande.getNumDemande() + "au statut" + Constant.ETATDEM_SUPPRIMEE);
                service.changeState(demande, Constant.ETATDEM_SUPPRIMEE);
            }
            stepContribution.setExitStatus(ExitStatus.COMPLETED);
        } catch (DemandeCheckingException e) {
            log.error("Erreur lors du passage à statut supprimé de ChangeInDeletedStatusAllDemandesExempFinishedForMoreThanThreeMonthsTasklet"
                    + e);
            stepContribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("demandes", this.demandes);
        }
        return stepExecution.getExitStatus();
    }
}
