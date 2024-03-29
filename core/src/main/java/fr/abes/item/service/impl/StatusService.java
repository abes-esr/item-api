package fr.abes.item.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.CommException;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.item.constant.Constant;
import fr.abes.item.service.IStatusService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Slf4j
@Service
public class StatusService implements IStatusService {
    @Getter
    private ProcessCBS cbs;

    @Value("${sudoc.login}")
    private String login;

    @Value("${sudoc.serveur}")
    private String serveurSudoc;

    @Value("${sudoc.port}")
    private String portSudoc;

    @Autowired
    private DataSource baseXmlDataSource;

    @Autowired
    private JdbcTemplate kopyaJdbcTemplate;

    public StatusService() {
        this.cbs = new ProcessCBS();
    }
    /**
     * Service sondant le status de la connexion au CBS
     * @return true si le client CBS repond, false dans le cas contraire
     */
    @Override
    public Boolean getCbsConnectionStatus(){
        try {
            cbs.authenticate(serveurSudoc, portSudoc, login, Constant.PASSSUDOC);
            return true;
        } catch (CBSException | CommException e) {
            log.error("serveur " + serveurSudoc + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * @return true si la requete SQL a fonctionné, ce qui signifie que la base XML est opérationnelle
     * false si la connection à la base XML à échoué
     */
    @Override
    public Boolean getXmlConnectionStatus(){
        JdbcTemplate jdbcTemplateBaseXml;
        jdbcTemplateBaseXml = new JdbcTemplate(baseXmlDataSource);

        try {
            SqlRowSet objectTest = jdbcTemplateBaseXml.queryForRowSet("select current_date");
            return objectTest.first();
        } catch (DataAccessException e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * @return true si la requete SQL a fonctionné, ce qui signifie que la base ITEM est opérationnelle
     * false si la connection à la base ITEM à échoué
     */
    @Override
    public Boolean getKopyaDataBaseStatus(){
        try {
            this.kopyaJdbcTemplate.queryForRowSet("SELECT user FROM role limit 1"); //Micro requête pour un tps de réponse très rapide (juste première occurence)
            return true;
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String getStatusOfServices(){
        StringBuilder result = new StringBuilder();

        if(this.getCbsConnectionStatus()){
            result.append("OK;");
        }else{
            result.append("KO;");
        }

        if(this.getXmlConnectionStatus()){
            result.append("OK;");
        } else{
            result.append("KO;");
        }

        if(this.getKopyaDataBaseStatus()){
            result.append("OK;");
        } else{
            result.append("KO;");
        }

        return result.toString();
    }
}
