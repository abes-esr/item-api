package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.DemandeSupp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IDemandeSuppDao extends JpaRepository<DemandeSupp, Integer> {

    static final int PREPAREE = 2;
    static final int ENATTENTE = 5;
    static final int ARCHIVEE = 9;
    static final int SUPPRIMEE = 10;

    @Query("select d from DemandeSupp d where d.etatDemande.numEtat not in (:ARCHIVEE, :PREPAREE, :SUPPRIMEE)")
    List<DemandeSupp> getAllActiveDemandesModifForAdminExtended();
    @Query("SELECT d FROM DemandeSupp d WHERE d.etatDemande.numEtat = :ENATTENTE")
    List<DemandeSupp> findAllWaitingDemandes();
}
