package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.abes.item.core.entities.item.DemandeModif;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Getter
@Setter
@JsonTypeName("MODIF")
@NoArgsConstructor
public class DemandeModifWebDto extends DemandeWebDto {
    @JsonProperty("zone")
    private String zoneEtSousZone;
    @JsonProperty("traitement")
    private String traitement;

    public DemandeModifWebDto(Integer id, String rcr, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification, String zoneEtSousZone, String traitement) {
        super(id, rcr, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
        this.zoneEtSousZone = zoneEtSousZone;
        this.traitement = traitement;
    }

    public DemandeModifWebDto(DemandeModif demande) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateCreation = format.format(demande.getDateCreation());
        String dateModification = format.format(demande.getDateModification());
        this.id = demande.getId();
        this.rcr = demande.getRcr();
        this.etatDemande = demande.getEtatDemande().getLibelle();
        this.commentaire = demande.getCommentaire();
        this.pourcentageProgressionTraitement = demande.getPourcentageProgressionTraitement();
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.zoneEtSousZone = demande.getZone() + " " + demande.getSousZone();
        this.traitement = demande.getTraitement().getLibelle();
    }
}
