package fr.abes.item.batch.traitement.retoursudoc;

import fr.abes.item.batch.traitement.model.LigneFichierDto;
import fr.abes.item.batch.traitement.model.LigneFichierDtoSupp;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.exception.QueryToSudocException;
import org.springframework.stereotype.Component;

@Component
public class BatchRetourSudocMapper {

    public String map(Exception exception, Demande demande, LigneFichierDto ligneFichierDto) {
        if (exception instanceof QueryToSudocException queryToSudocException) {
            return mapQueryToSudocException(queryToSudocException, demande, ligneFichierDto);
        }
        return exception.getMessage();
    }

    String mapMissingSuppressionEpn(DemandeSupp demandeSupp, LigneFichierDtoSupp ligneFichierDtoSupp) {
        if (hasExplicitSuppressionEpnMappingContext(demandeSupp, ligneFichierDtoSupp)) {
            return Constant.ERR_FILE_EPN_INEXISTANT_OR_ERRONE;
        }
        return Constant.WARN_NOTICE_EPN_INEXISTANT;
    }

    private String mapQueryToSudocException(
            QueryToSudocException exception,
            Demande demande,
            LigneFichierDto ligneFichierDto
    ) {
        if (demande instanceof DemandeSupp demandeSupp
                && ligneFichierDto instanceof LigneFichierDtoSupp ligneFichierDtoSupp
                && hasExplicitSuppressionEpnMappingContext(demandeSupp, ligneFichierDtoSupp)
                && Constant.ERR_FILE_NOTICE_NOT_FOUND.equals(exception.getMessage())) {
            return Constant.ERR_FILE_EPN_INEXISTANT_OR_ERRONE;
        }
        return exception.getMessage();
    }

    private boolean hasExplicitSuppressionEpnMappingContext(
            DemandeSupp demandeSupp,
            LigneFichierDtoSupp ligneFichierDtoSupp
    ) {
        return demandeSupp.getTypeSuppression() == TYPE_SUPPRESSION.EPN
                && ligneFichierDtoSupp.getEpn() != null;
    }
}
