package fr.abes.item.dto;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DtoBuilder {
    private final List<Class<? extends DemandeWebDto>> dtoList;

    public DtoBuilder(List<Class<? extends DemandeWebDto>> dtoList) {
        this.dtoList = dtoList;
    }

    @PostConstruct
    void init() {
        dtoList.add(DemandeExempWebDto.class);
        dtoList.add(DemandeModifWebDto.class);
        dtoList.add(DemandeRecouvWebDto.class);
        dtoList.add(DemandeSuppWebDto.class);
    }

    public DemandeWebDto buildDemandeDto(Demande demande, TYPE_DEMANDE type) {
        return switch (type) {
            case EXEMP -> new DemandeExempWebDto((DemandeExemp) demande);
            case MODIF -> new DemandeModifWebDto((DemandeModif) demande);
            case RECOUV -> new DemandeRecouvWebDto((DemandeRecouv) demande);
            case SUPP -> new DemandeSuppWebDto((DemandeSupp) demande);
        };
    }

    public UtilisateurWebDto buildUtilisateurDto(Utilisateur utilisateur) {
        return new UtilisateurWebDto(utilisateur.getId(), utilisateur.getEmail());
    }
}
