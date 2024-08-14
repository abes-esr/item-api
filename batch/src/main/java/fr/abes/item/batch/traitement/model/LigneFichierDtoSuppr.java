package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichierSupp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class LigneFichierDtoSuppr extends LigneFichierDto implements ILigneFichierDtoService {
    @Getter @Setter
    private String requete;
    @Getter @Setter
    private String ppn;
    @Getter @Setter
    private String rcr;
    @Getter @Setter
    private String epn;

    public LigneFichierDtoSuppr(LigneFichierSupp ligneFichierSupp){
        super(ligneFichierSupp.getNumLigneFichier(), ligneFichierSupp.getTraitee(), ligneFichierSupp.getPosition(), ligneFichierSupp.getId(), ligneFichierSupp.getRetourSudoc(), ligneFichierSupp.getValeurZone());
        this.epn = ligneFichierSupp.getEpn();
        this.ppn = ligneFichierSupp.getPpn();
        this.rcr = ligneFichierSupp.getRcr();
    }

    public LigneFichierDtoSuppr(Integer numLigneFichier, Integer traitee, Integer position, Integer refDemande, String retourSudoc, String valeurZone, String requete, String ppn, String rcr, String epn) {
        super(numLigneFichier, traitee, position, refDemande, retourSudoc, valeurZone);
        this.requete = requete;
        this.ppn = ppn;
        this.rcr = rcr;
        this.epn = epn;
    }

    @Override
    public String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult) {
        return null;
    }

    @Override
    public TYPE_DEMANDE getTypeDemande() {
        return null;
    }
}
