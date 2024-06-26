package fr.abes.item.batch.webstats;

import com.opencsv.CSVWriter;
import fr.abes.item.batch.LogTime;
import fr.abes.item.core.constant.Constant;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

@Slf4j
public class ExportStatistiquesTasklet implements Tasklet, StepExecutionListener {
    @Autowired
    protected DataSource itemDataSource;

    @Autowired
    protected JdbcTemplate itemJdbcTemplate;

    private Integer annee;
    private Integer mois;
    private List<NbDemandesTraiteesDto> listeDemandesTraitees;
    private List<NbExemplairesTraitesDto> listeExemplairesTraites;


    @Value("${files.upload.statistiques.path}")
    private String uploadPath;


    @Override
    public void beforeStep(StepExecution stepExecution) {
            Date dateDebut = (Date) stepExecution.getJobExecution().getExecutionContext().get("dateDebut");
            Date dateFin = (Date) stepExecution.getJobExecution().getExecutionContext().get("dateFin");
            annee = (int) stepExecution.getJobExecution().getExecutionContext().get("annee");
            mois = (int) stepExecution.getJobExecution().getExecutionContext().get("mois");
            listeDemandesTraitees = getNbDemandesTraitees(dateDebut, dateFin);
            listeExemplairesTraites = getNbExemplairesTraites(dateDebut, dateFin);
           }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilename(Constant.STAT_NBDEMANDESTRAITEES_FILENAME)), ';', CSVWriter.NO_QUOTE_CHARACTER)){
            for (NbDemandesTraiteesDto demande : listeDemandesTraitees) {
                writer.writeNext(new String[]{demande.getRcr(), demande.getNbDemandesTraitees().toString()});
            }
        } catch (Exception e) {
            log.error(Constant.ERROR_OPENING_FILE_FOR_NUMBER_OF_REQUESTS_PROCESSED_BY_RCR);
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilename(Constant.STAT_NBEXEMPLAIRESTRAITES_FILENAME)), ';', CSVWriter.NO_QUOTE_CHARACTER)) {
            for (NbExemplairesTraitesDto exemp : listeExemplairesTraites) {
                writer.writeNext(new String[]{exemp.getRcr(), exemp.getTypeTraitement().toString(), exemp.getNbExemplaires().toString()});
            }
        } catch (Exception e) {
            log.error(Constant.ERROR_OPENING_FILE_FOR_NUMBER_OF_EXEMPLARIES_PROCESSES_BY_RCR_AND_TREATMENT);
        }
        return RepeatStatus.FINISHED;
    }

    private List<NbDemandesTraiteesDto> getNbDemandesTraitees(Date dateDebut, Date dateFin) {
        String query = "select RCR, count(*) from DEMANDE_MODIF d, JOURNAL_DEMANDE_MODIF j where j.JOU_DEM_ID = d.NUM_DEMANDE and j.JOU_ETA_ID=7 and j.DATE_ENTREE between ? and ? group by d.RCR";
        return itemJdbcTemplate.query(query, new Object[] {dateDebut, dateFin}, new NbDemandesTraiteesMapper());
    }

    private List<NbExemplairesTraitesDto> getNbExemplairesTraites(Date dateDebut, Date dateFin) {
        String query = "select d.DEM_TRAIT_ID, d.RCR, count(*) " +
                "from JOURNAL_DEMANDE_MODIF j, DEMANDE_MODIF d, LIGNE_FICHIER_MODIF lf " +
                "where j.DATE_ENTREE between ? and ? "+
                "and j.JOU_ETA_ID=6 and j.JOU_DEM_ID = d.NUM_DEMANDE and d.NUM_DEMANDE = lf.REF_DEMANDE and lf.TRAITEE=1 " +
                "group by d.DEM_TRAIT_ID, d.RCR";
        return itemJdbcTemplate.query(query, new Object[] {dateDebut, dateFin}, new NbExemplairesTraitesMapper());
    }

    private String getFilename(String filename) {
        return uploadPath + annee + ((mois < 10) ? '0' + mois.toString() : mois.toString()) + "_" + filename + Constant.EXTENSIONCSV;
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        LogTime.logFinTraitement(stepExecution);
        return null;
    }
}
