package fr.abes.item.batch.traitement;

import fr.abes.item.batch.LogTime;
import fr.abes.item.batch.mail.IMailer;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
import fr.abes.item.batch.traitement.model.LigneFichierDtoExemp;
import fr.abes.item.batch.traitement.model.LigneFichierDtoModif;
import fr.abes.item.batch.traitement.model.LigneFichierDtoRecouv;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ILigneFichierService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LireLigneFichierTasklet implements Tasklet, StepExecutionListener {
    private final StrategyFactory factory;
    private final List<LigneFichierDto> lignesFichier;
    private final String mailAdmin;
    private ILigneFichierService ligneFichierService;
    private IDemandeService demandeService;
    private LocalDateTime dateDebut;
    private Integer demandeId;
    private TYPE_DEMANDE typeDemande;
    private Demande demande;
    private String email;

    IMailer mailer;

    public LireLigneFichierTasklet(StrategyFactory factory, String mailAdmin) {
        this.factory = factory;
        this.mailAdmin = mailAdmin;
        lignesFichier = new ArrayList<>();
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        LogTime.logDebutTraitement(stepExecution);
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.typeDemande = TYPE_DEMANDE.valueOf((String) executionContext.get("typeDemande"));
        this.demandeId = (Integer) executionContext.get("demandeId");
        demandeService = factory.getStrategy(IDemandeService.class, this.typeDemande);
        this.demande = demandeService.findById(demandeId);
        ligneFichierService = factory.getStrategy(ILigneFichierService.class, demande.getTypeDemande());
        this.email = demande.getUtilisateur().getEmail() + ";" + mailAdmin;
        mailer = factory.getStrategy(IMailer.class, demande.getTypeDemande());
        this.dateDebut = stepExecution.getJobExecution().getCreateTime();
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception{
        log.warn(Constant.ENTER_EXECUTE_FROM_LIRELIGNEFICHIERTASKLET);
        try {
            for (LigneFichier localLigne : ligneFichierService.getLigneFichierbyDemande(demande)) {
                if (localLigne.getTraitee().equals(0)) {
                    switch (demande.getTypeDemande()){
                        case EXEMP:
                            this.lignesFichier.add(new LigneFichierDtoExemp((LigneFichierExemp) localLigne));
                            break;
                        case MODIF:
                            this.lignesFichier.add(new LigneFichierDtoModif((LigneFichierModif) localLigne));
                            break;
                        case RECOUV:
                            this.lignesFichier.add(new LigneFichierDtoRecouv((LigneFichierRecouv) localLigne));
                            break;
                        default:
                    }
                }
            }
        } catch (JDBCConnectionException | ConstraintViolationException j) {
            log.error("Erreur hibernate JDBC");
            log.error(j.toString());
        } catch (DataAccessException e) {
            if(e.getRootCause() instanceof SQLException sqlEx){
                log.error("Erreur SQL : " + sqlEx.getErrorCode());
                log.error(sqlEx.getSQLState() + "|" + sqlEx.getMessage() + "|" + sqlEx.getLocalizedMessage());
            }

            log.error(Constant.ERROR_ACCESS_DATABASE + e);
            mailer.mailEchecTraitement(
                    this.email,
                    this.demande,
                    this.dateDebut);
            mailer.mailAlertAdmin(this.mailAdmin, demande);
            demandeService.changeState(demande, Constant.ETATDEM_ERREUR);
            stepContribution.setExitStatus(ExitStatus.FAILED);

        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepExecution.getJobExecution().getExecutionContext().put("lignes", this.lignesFichier);
            stepExecution.getJobExecution().getExecutionContext().put("demandeId", this.demandeId);
            stepExecution.getJobExecution().getExecutionContext().put("typeDemande", this.typeDemande.toString());
        }
        return stepExecution.getExitStatus();
    }

}
