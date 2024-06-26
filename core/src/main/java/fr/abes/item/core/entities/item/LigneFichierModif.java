package fr.abes.item.core.entities.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@Table(name= "LIGNE_FICHIER_MODIF")
@Getter @Setter
public class LigneFichierModif extends LigneFichier implements Serializable, ILigneFichier {
    @Column(name="PPN")
    private String ppn;
    @Column(name="RCR")
    private String rcr;
    @Column(name="EPN")
    private String epn;
    @ManyToOne
    @JoinColumn(name = "REF_DEMANDE") @NotNull
    private DemandeModif demandeModif;

    @SuppressWarnings("squid:S00107")
    public LigneFichierModif(String ppn,
                             String rcr, String epn, String valeurZone, Integer position,
                             Integer traitee, String retourSudoc, DemandeModif demandeModif) {
        super(valeurZone, traitee, position, retourSudoc);
        this.ppn = ppn;
        this.rcr = rcr;
        this.epn = epn;
        this.demandeModif = demandeModif;
    }

    @Override
    public void setEntityAfterBatch(LigneFichier ligneFichier) {
        LigneFichierModif ligneFichierModif = (LigneFichierModif) ligneFichier;
        this.setRetourSudoc(ligneFichierModif.getRetourSudoc());
        this.setTraitee(1);
    }
}
