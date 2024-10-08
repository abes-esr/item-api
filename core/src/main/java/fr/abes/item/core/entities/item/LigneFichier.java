package fr.abes.item.core.entities.item;

import fr.abes.item.core.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@NoArgsConstructor
@Setter @Getter
public abstract class LigneFichier implements GenericEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NUM_LIGNEFICHIER")
    protected Integer numLigneFichier;
    @Column(name="TRAITEE")
    protected Integer traitee;
    @Column(name="POS")
    protected Integer position;
    @Column(name="RETOUR_SUDOC")
    protected String retourSudoc;


    public LigneFichier(Integer traitee, Integer position, String retourSudoc) {
        this.traitee = traitee;
        this.position = position;
        this.retourSudoc = retourSudoc;
    }

    @Override
    public Integer getId() { return this.numLigneFichier; }
}
