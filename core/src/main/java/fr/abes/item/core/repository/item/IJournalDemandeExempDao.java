package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.JournalDemandeExemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IJournalDemandeExempDao extends JpaRepository<JournalDemandeExemp, Integer> {
    List<JournalDemandeExemp> findAllByDemandeExemp_NumDemandeOrderByDateEntreeDesc(Integer numDemande);
}
