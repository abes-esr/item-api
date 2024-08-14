package fr.abes.item.core.entities.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name="LIGNE_FICHIER_SUPP")
public class LigneFichierSupp extends LigneFichier implements Serializable, ILigneFichier {
    @Column(name = "PPN")
    private String ppn;

    @Column(name = "RCR")
    private String rcr;

    @Column(name = "EPN")
    private String epn;

    @Column(name = "NB_REPONSE")
    private Integer nbReponse;

    @ManyToOne
    @JoinColumn(name = "REF_DEMANDE") @NotNull
    private DemandeSupp demandeSupp;


    @Override
    public void setEntityAfterBatch(LigneFichier ligneFichier) {
        //TODO revoir
        LigneFichierSupp ligneFichierSupp = (LigneFichierSupp)ligneFichier;
        this.setRetourSudoc(ligneFichierSupp.getRetourSudoc());
        this.setNbReponse(ligneFichierSupp.getNbReponse());
        this.setTraitee(1);
    }
}
