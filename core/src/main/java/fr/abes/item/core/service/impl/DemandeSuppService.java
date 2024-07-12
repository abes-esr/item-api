package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.components.Fichier;
import fr.abes.item.core.components.FichierEnrichiSupp;
import fr.abes.item.core.components.FichierInitial;
import fr.abes.item.core.components.FichierPrepare;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeSuppDao;
import fr.abes.item.core.service.FileSystemStorageService;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.SUPP})
public class DemandeSuppService extends DemandeService implements IDemandeService {
    private final IDemandeSuppDao demandeSuppDao;
    private final FileSystemStorageService storageService;

    @Value("${files.upload.path}")
    private String uploadPath;

    public DemandeSuppService(ILibProfileDao libProfileDao, IDemandeSuppDao demandeSuppDao, FileSystemStorageService storageService) {
        super(libProfileDao);
        this.demandeSuppDao = demandeSuppDao;
        this.storageService = storageService;
    }

    @Override
    public Demande save(Demande entity) {
        return null;
    }

    @Override
    public Demande findById(Integer id) {
        Optional<DemandeSupp> demandeSupp = demandeSuppDao.findById(id);
        demandeSupp.ifPresent(this::setIlnShortNameOnDemande);
        return demandeSupp.orElse(null);
    }

    @Override
    public Demande creerDemande(String rcr, Integer userNum) {
        return null;
    }

    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getId();
        /*Préparation du fichier initial rattaché à la demande de suppression */
        FichierInitial fichierInit = (FichierInitial) FichierFactory.getFichier(Constant.ETATDEM_PREPARATION, TYPE_DEMANDE.SUPP);
        fichierInit.generateFileName(numDemande);
        fichierInit.setPath(Paths.get(uploadPath + "supp/" + numDemande));
        /*Préparation du fichier enrichi suite l'appel à la fonction oracle */
        FichierPrepare fichierPrepare = (FichierPrepare) FichierFactory.getFichier(Constant.ETATDEM_PREPAREE, TYPE_DEMANDE.SUPP);
        fichierPrepare.generateFileName(numDemande);
        fichierPrepare.setPath(Paths.get(uploadPath + "supp/" + numDemande));
        /*Préparation du fichier enrichi par l'utilisateur */
        FichierEnrichiSupp fichierEnrichiSupp = (FichierEnrichiSupp) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.SUPP);
        fichierEnrichiSupp.generateFileName(numDemande);
        fichierEnrichiSupp.setPath(Paths.get(uploadPath + "supp/" + numDemande));

    }

    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        Integer numDemande = demande.getNumDemande();
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.SUPP);
            fichier.generateFileName(numDemande);
            stockerFichierOnDisk(file, fichier, (DemandeSupp) demande);
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }
    }

    private void stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeSupp demande) throws IOException, FileCheckingException, DemandeCheckingException {
        Integer numDemande = demande.getNumDemande();
        try {
            storageService.changePath(Paths.get(uploadPath + "supp/" + numDemande));
            storageService.init();
            storageService.store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + "supp/" + numDemande));
            fichier.checkFileContent(demande);
            //suppression des lignes vides d'un fichier initial de ppn / epn
            if (fichier.getType() == Constant.ETATDEM_PREPARATION) {
                FichierInitial fichierInitial = (FichierInitial) fichier;
                fichierInitial.supprimerRetourChariot();
            }
            checkEtatDemande(demande);
        } catch (FileCheckingException e) {
            storageService.delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    private void checkEtatDemande(DemandeSupp demande) throws DemandeCheckingException {
        int etat = demande.getEtatDemande().getNumEtat();
        switch (etat) {
            case Constant.ETATDEM_PREPARATION -> preparerFichierEnPrep(demande);
            case Constant.ETATDEM_PREPAREE -> changeState(demande, Constant.ETATDEM_ACOMPLETER);
            case Constant.ETATDEM_ACOMPLETER -> {
                changeState(demande, Constant.ETATDEM_SIMULATION);
            }
        }
    }

    private void preparerFichierEnPrep(DemandeSupp demande) {

    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException, IOException {
        return null;
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public List<Demande> getActiveDemandesForUser(String iln) {
        return null;
    }

    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        return null;
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return null;
    }

    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        return null;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeSupp> demandeSupps = demandeSuppDao.getAllActiveDemandesModifForAdminExtended();
        List<Demande> demandesList = new ArrayList<>(demandeSupps);
        setIlnShortNameOnList(demandesList);
        return demandesList;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        return null;
    }

    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws CBSException, ZoneException, IOException {
        return new String[0];
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToArchive() {
        return null;
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToPlaceInDeletedStatus() {
        return null;
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToDelete() {
        return null;
    }

    @Override
    public String getQueryToSudoc(String code, String type, String[] valeurs) throws QueryToSudocException {
        return null;
    }
}
