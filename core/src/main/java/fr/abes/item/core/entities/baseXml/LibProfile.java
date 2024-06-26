package fr.abes.item.core.entities.baseXml;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Le schéma est obligatoire ici
 * car les dao de la base Item doivent pouvoir accéder via les requêtes aux tables de la base XML
 */
@Entity
@Table(name = "LIB_PROFILE", schema = "AUTORITES")
@Getter
@Setter
@NoArgsConstructor
public class LibProfile {
    @Id
    @Column(name = "RCR")
    private String rcr;

    @Column(name = "SHORT_NAME")
    private String shortName;

    @Column(name = "ILN")
    private String iln;

}
