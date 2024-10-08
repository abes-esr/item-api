package fr.abes.item.batch.traitement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class LigneFichierDto implements Serializable, ILigneFichierDtoService {
    private Integer numLigneFichier;
    private Integer traitee;
    private Integer position;
    private Integer refDemande;
    private String retourSudoc;

    LigneFichierDto(Integer numLigneFichier, Integer traitee, Integer position, Integer refDemande, String retourSudoc) {
        this.numLigneFichier = numLigneFichier;
        this.traitee = traitee;
        this.position = position;
        this.refDemande = refDemande;
        this.retourSudoc = retourSudoc;
    }

    public abstract TYPE_DEMANDE getTypeDemande();
}
