package fr.abes.item.dao.item;

import fr.abes.item.entities.item.JournalDemandeExemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IJournalDemandeExempDao extends JpaRepository<JournalDemandeExemp, Integer> {
    @Query("delete from JournalDemandeExemp where demandeExemp.numDemande = :demandeExemp")
    void deleteAllLinesJournalDemandeExempByDemandeId(@Param("demandeExemp") Integer demandeExemp);
}
