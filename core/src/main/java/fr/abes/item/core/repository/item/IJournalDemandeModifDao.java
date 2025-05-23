package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.JournalDemandeModif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IJournalDemandeModifDao extends JpaRepository<JournalDemandeModif, Integer> {
    List<JournalDemandeModif> findAllByDemandeModif_NumDemandeOrderByDateEntreeDesc(Integer numDemande);
}
