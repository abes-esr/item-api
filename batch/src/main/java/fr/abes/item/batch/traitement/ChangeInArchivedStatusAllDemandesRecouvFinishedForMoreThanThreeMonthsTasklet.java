package fr.abes.item.batch.traitement;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.service.impl.DemandeRecouvService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class ChangeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    DemandeRecouvService demandeRecouvService;

    List<DemandeRecouv> demandes;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {}

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        log.warn("entrée dans execute de ChangeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet...");
        try {
            this.demandes = demandeRecouvService.getIdNextDemandeToArchive();
            if (this.demandes == null) {
                log.warn(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            //Iteration sur chaque demande pour en modifier le statut
            for (Demande demande : this.demandes) {
                log.info("Passage de la demande de recouvrement " + demande.getNumDemande() + "au statut" + Constant.ETATDEM_ARCHIVEE);
                demandeRecouvService.changeState(demande, Constant.ETATDEM_ARCHIVEE);
            }
            stepContribution.setExitStatus(ExitStatus.COMPLETED);
        } catch (DemandeCheckingException e) {
            log.error("Erreur lors du passage à statut archivé de ChangeInArchivedStatusAllDemandesRecouvFinishedForMoreThanThreeMonthsTasklet"
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