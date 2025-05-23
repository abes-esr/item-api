package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.JournalDemandeSupp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IJournalDemandeSuppDao extends JpaRepository<JournalDemandeSupp, Integer> {
    List<JournalDemandeSupp> findAllByDemandeSupp_NumDemandeOrderByDateEntreeDesc(Integer numDemande);
}
