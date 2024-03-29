package fr.abes.item.dao.item;

import fr.abes.item.entities.item.JournalDemandeModif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IJournalDemandeModifDao extends JpaRepository<JournalDemandeModif, Integer> {
    @Query("delete from JournalDemandeModif where demandeModif.numDemande = :demandeModif")
    void deleteAllLinesJournalDemandeModifByDemandeId(@Param("demandeModif") Integer demandeModif);
}
