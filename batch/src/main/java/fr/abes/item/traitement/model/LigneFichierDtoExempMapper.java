package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.entities.item.LigneFichierExemp;
import fr.abes.item.service.factory.Strategy;

@Strategy(type = ILigneFichierDtoMapper.class, typeDemande = TYPE_DEMANDE.EXEMP)
public class LigneFichierDtoExempMapper implements ILigneFichierDtoMapper {
    @Override
    public LigneFichier getLigneFichierEntity(LigneFichierDto lfd)
    {
        LigneFichierDtoExemp lfdExemp = (LigneFichierDtoExemp) lfd;
        LigneFichierExemp lf = new LigneFichierExemp();
        lf.setNumLigneFichier(lfdExemp.getNumLigneFichier());
        lf.setIndexRecherche(lfdExemp.getIndexRecherche());
        lf.setValeurZone(lfdExemp.getValeurZone());
        lf.setTraitee(lfdExemp.getTraitee());
        lf.setPosition(lfdExemp.getPosition());
        lf.setRetourSudoc(lfdExemp.getRetourSudoc());
        lf.setNumExemplaire(lfdExemp.getNumExemplaire());
        lf.setL035(lfdExemp.getL035());
        lf.setNbReponse(lfdExemp.getNbReponses());
        lf.setListePpn(lfdExemp.getListePpn());
        lf.setDemandeExemp(new DemandeExemp(lfdExemp.getRefDemande()));
        return lf;
    }

    private LigneFichierDtoExempMapper(){}
}
