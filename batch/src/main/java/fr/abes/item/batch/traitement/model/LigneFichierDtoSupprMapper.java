package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierSupp;

@Strategy(type = ILigneFichierDtoMapper.class, typeDemande = TYPE_DEMANDE.EXEMP)
public class LigneFichierDtoSupprMapper implements ILigneFichierDtoMapper {
    @Override
    public LigneFichierSupp getLigneFichierEntity(LigneFichierDto lfd)
            //TODO ajuster le mapper en fonction de ce que renvoie le sudoc
    {
        LigneFichierDtoSuppr lfdSuppr = (LigneFichierDtoSuppr) lfd;
        LigneFichierSupp lf = new LigneFichierSupp();
        lf.setPpn(lfdSuppr.getPpn());
        lf.setRcr(lfdSuppr.getRcr());
        lf.setEpn(lfdSuppr.getEpn());
        lf.setDemandeSupp(new DemandeSupp(lfdSuppr.getRefDemande()));
        lf.setTraitee(lfdSuppr.getTraitee());
        return lf;
    }
}
