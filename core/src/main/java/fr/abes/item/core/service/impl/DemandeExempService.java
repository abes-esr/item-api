package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.cbs.notices.ZoneEtatColl;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.item.core.components.Fichier;
import fr.abes.item.core.components.FichierEnrichiExemp;
import fr.abes.item.core.configuration.factory.FichierFactory;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeExempDao;
import fr.abes.item.core.repository.item.ILigneFichierExempDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import fr.abes.item.core.service.*;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ToString
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.EXEMP})
public class DemandeExempService extends DemandeService implements IDemandeService {
    private FichierEnrichiExemp fichierEnrichiExemp;

    private final IDemandeExempDao demandeExempDao;
    private final FileSystemStorageService storageService;
    private final ILigneFichierService ligneFichierService;
    private final ReferenceService referenceService;
    private final JournalService journalService;
    private final TraitementService traitementService;
    private final UtilisateurService utilisateurService;
    private final IZonesAutoriseesDao zonesAutoriseesDao;
    private final ILigneFichierExempDao ligneFichierExempDao;

    @Value("${files.upload.path}")
    private String uploadPath;

    @Getter
    private String exemplairesExistants;

    @Getter
    private String donneeLocaleExistante;

    @Getter
    private int nbReponses;

    public DemandeExempService(ILibProfileDao libProfileDao, IDemandeExempDao demandeExempDao, FileSystemStorageService storageService, LigneFichierExempService ligneFichierExempService, ReferenceService referenceService, JournalService journalService, TraitementService traitementService, UtilisateurService utilisateurService, IZonesAutoriseesDao zonesAutoriseesDao, ILigneFichierExempDao ligneFichierExempDao) {
        super(libProfileDao);
        this.demandeExempDao = demandeExempDao;
        this.storageService = storageService;
        this.ligneFichierService = ligneFichierExempService;
        this.referenceService = referenceService;
        this.journalService = journalService;
        this.traitementService = traitementService;
        this.utilisateurService = utilisateurService;
        this.zonesAutoriseesDao = zonesAutoriseesDao;
        this.ligneFichierExempDao = ligneFichierExempDao;
    }

    public List<Demande> findAll() {
        List<Demande> liste = new ArrayList<>(demandeExempDao.findAll());
        setIlnShortNameOnList(new ArrayList<>(liste));
        return liste;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        List<DemandeExemp> demandeExemps = demandeExempDao.getAllActiveDemandesExempForAdmin(iln);
        List<Demande> demandeList = new ArrayList<>(demandeExemps);
        //TODO 1 chopper les rcr en une iste string, 2 dao xml pour recuperer la liste des libelle avec un tableau mappé, 3 alimenter les entites LIB iteration

        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeExemp> demandeExemp = demandeExempDao.getAllActiveDemandesExempForAdminExtended();
        List<Demande> demandeList = new ArrayList<>(demandeExemp);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    public String getLibelleTypeExempDemande(Integer idDemande) {
        return demandeExempDao.getTypeExemp(idDemande).getLibelle();
    }

    @Override
    public Demande save(Demande entity) {
        DemandeExemp demande = (DemandeExemp) entity;
        demande.setDateModification(Calendar.getInstance().getTime());
        DemandeExemp demandeSaved = demandeExempDao.save(demande);
        demandeSaved.setShortname(entity.getShortname());
        return demandeSaved;
    }

    @Override
    public DemandeExemp findById(Integer id) {
        Optional<DemandeExemp> demandeExemp = demandeExempDao.findById(id);
        /*On contrôle si la demande est présente*/
        demandeExemp.ifPresent(this::setIlnShortNameOnDemande);
        return demandeExemp.orElse(null);
    }

    @Override
    public void deleteById(Integer id) {
        //suppression des fichiers et du répertoire
        storageService.changePath(Paths.get(uploadPath + "exemp/" + id));
        storageService.deleteAll();
        demandeExempDao.deleteById(id);
    }

    /**
     * Méthode permettant de chercher les demandeModifs d'un utilisateur
     *
     * @param iln numéro de l'utilisateur propriétaire des demandeModifs
     * @return liste des demandeModifs de l'utilisateur (hors demandeModifs archivées)
     */
    @Override
    public List<Demande> getActiveDemandesForUser(String iln) {
        List<DemandeExemp> demandeExemps = demandeExempDao.getActiveDemandesExempForUserExceptedPreparedStatus(iln);
        List<Demande> listeDemande = new ArrayList<>(demandeExemps);
        setIlnShortNameOnList(listeDemande);
        return listeDemande;
    }

    public boolean hasDonneeLocaleExistante() {
        return !donneeLocaleExistante.isEmpty();
    }

    /**
     * mise à jour du type d'exemplarisation en fonction de l'option choisie coté front
     *
     * @param demandeId identifiant de la demande
     * @param typeExempId valeur du type d'exemplarisation
     * @return la demande modifiée
     */
    public Demande majTypeExemp(Integer demandeId, Integer typeExempId) {
        DemandeExemp demandeExemp = this.findById(demandeId);
        TypeExemp typeExemp = referenceService.findTypeExempById(typeExempId);
        if (demandeExemp != null) {
            demandeExemp.setDateModification(Calendar.getInstance().getTime());
            demandeExemp.setTypeExemp(typeExemp);
            demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER));
            return this.save(demandeExemp);
        }
        return null;
    }

    /**
     * vérification du fichier et création de l'objet correspondant
     *
     * @param file    fichier issu du front
     * @param demande demande concernée
     * @throws IOException              : erreur lecture fichier
     * @throws FileTypeException        : erreur de type de fichier en entrée
     * @throws FileCheckingException    : erreur dans la vérification de l'extension du fichier
     * @throws DemandeCheckingException : erreur dans l'état de la demande
     */
    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        try {
            Utilitaires.checkExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Fichier fichier = FichierFactory.getFichier(demande.getEtatDemande().getNumEtat(), TYPE_DEMANDE.EXEMP); //Retourne un FichierEnrichiExemp
            fichier.generateFileName(demande); //génération nom du fichier
            stockerFichierOnDisk(file, fichier, demandeExemp); //stockage du fichier sur disque, le controle de l'entête du fichier s'effectue ici
            this.majDemandeWithFichierEnrichi(demandeExemp); //mise à jour de la demande avec les paramètres du fichier enrichi : index de recherche, liste des zones, ajout des lignes du fichier dans la BDD
        } catch (NullPointerException e) {
            throw new NullPointerException(Constant.ERR_FILE_NOT_FOUND);
        }
    }

    /**
     * Stockage physique du fichier de la demande sur le disque
     *
     * @param file         fichier à sauvegarder issu du client
     * @param fichier      objet correspondant au fichier
     * @param demandeExemp demande rattachée au fichier
     * @throws IOException : erreur lecture fichier
     * @throws FileCheckingException : erreur vérification fichier
     */
    private void stockerFichierOnDisk(MultipartFile file, Fichier fichier, DemandeExemp demandeExemp) throws IOException, FileCheckingException {
        Integer numDemande = demandeExemp.getId();
        try {
            storageService.changePath(Paths.get(uploadPath + "exemp/" + numDemande));
            storageService.init();
            storageService.store(file, fichier.getFilename());
            fichier.setPath(Paths.get(uploadPath + "exemp/" + numDemande));
            //Ici l'objet fichierExemp va etre renseigné avec les zones courante et valeur de ces zones
            fichier.checkFileContent(demandeExemp); //Contrôle de l'entête et contenu du fichier
        } catch (FileCheckingException e) {
            storageService.delete(fichier.getFilename());
            throw e;
        } catch (IOException e) {
            throw new IOException(Constant.ERR_FILE_STORAGE_FILE_UNREADABLE);
        }
    }

    /**
     * mise à jour de la demande avec les paramètres du fichier enrichi : index de recherche, liste des zones, ajout des lignes du fichier dans la BDD
     *
     * @param demandeExemp demande concernée
     * @throws IOException : erreur lecture fichier
     * @throws DemandeCheckingException : erreur dans la demande
     */
    private void majDemandeWithFichierEnrichi(DemandeExemp demandeExemp) throws IOException, DemandeCheckingException {
        demandeExemp.setIndexRecherche(fichierEnrichiExemp.getIndexRecherche()); //Index de recherche
        demandeExemp.setListeZones(fichierEnrichiExemp.getValeurZones()); //Ligne d'entête sans l'index de recherche
        ligneFichierService.saveFile(storageService.loadAsResource(fichierEnrichiExemp.getFilename()).getFile(), demandeExemp); //Construction des lignes d'exemplaires pour insertion en base sur table LIGNE_FICHIER_EXEMP
        changeState(demandeExemp, Constant.ETATDEM_SIMULATION);
    }

    /**
     * Méthode permettant de générer le fichier initial sur le disque
     *
     * @param demande demande concernant le fichier
     * @throws FileTypeException : état de la demande ou type de demande rendant impossible la fourniture du fichier par la factory
     */
    @Override
    public void initFiles(Demande demande) throws FileTypeException {
        Integer numDemande = demande.getId();
        // préparation du fichier envoyé par l'utilisateur
        fichierEnrichiExemp = (FichierEnrichiExemp) FichierFactory.getFichier(Constant.ETATDEM_ACOMPLETER, TYPE_DEMANDE.EXEMP); //création d'un objet fichier
        fichierEnrichiExemp.generateFileName(demande);  //creation du nom du fichier (fichierenrichi)
        fichierEnrichiExemp.setPath(Paths.get(uploadPath + "exemp/" + numDemande)); //emplacement du dossier ou sera crée le fichier
    }

    /**
     * Méthode permettant de changer l'état d'une demande
     *
     * @param demande     demande sur laquelle appliquer le changement d'état
     * @param etatDemande état vers lequel la demande doit être passée
     * @return demande modifiée
     * @throws DemandeCheckingException si la demande n'est pas dans l'état précédent à celui passé en paramètre
     */
    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        if ((etatDemande == Constant.ETATDEM_ERREUR) || (demande.getEtatDemande().getNumEtat() == getPreviousState(etatDemande))) {
            EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
            demande.setEtatDemande(etat);
            journalService.addEntreeJournal((DemandeExemp) demande, etat);
            return this.save(demande);
        } else {
            throw new DemandeCheckingException(Constant.DEMANDE_IS_NOT_IN_STATE + getPreviousState(etatDemande));
        }
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        EtatDemande etat = referenceService.findEtatDemandeById(etatDemande);
        demande.setEtatDemande(etat);
        journalService.addEntreeJournal((DemandeExemp) demande, etat);
        return this.save(demande);
    }

    /**
     * Retourne l'état précédent d'une demande dans le déroulement normal du processus
     *
     * @param etatDemande : etat dont on souhaite connaitre l'état précédent
     * @return : l'état précédent dans le processus
     */
    private int getPreviousState(int etatDemande) {
        return switch (etatDemande) {
            case Constant.ETATDEM_SIMULATION -> Constant.ETATDEM_ACOMPLETER;
            case Constant.ETATDEM_ACOMPLETER -> Constant.ETATDEM_PREPARATION;
            case Constant.ETATDEM_ATTENTE -> Constant.ETATDEM_SIMULATION;
            case Constant.ETATDEM_ENCOURS -> Constant.ETATDEM_ATTENTE;
            case Constant.ETATDEM_TERMINEE -> Constant.ETATDEM_ENCOURS;
            case Constant.ETATDEM_ERREUR -> Constant.ETATDEM_ERREUR;
            case Constant.ETATDEM_ARCHIVEE -> Constant.ETATDEM_TERMINEE;
            case Constant.ETATDEM_SUPPRIMEE -> Constant.ETATDEM_ARCHIVEE;
            default -> 0;
        };
    }

    /**
     * Méthode permettant de changer l'état d'une demande vers l'état précédent dans le processus global de l'application
     *
     * @param demande : demande concernée
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un état incorrect
     */
    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException {
        int etatDemande = demande.getEtatDemande().getId();
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        if (etatDemande == Constant.ETATDEM_ACOMPLETER) {
            demandeExemp.setTypeExemp(null);
            demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION));
            save(demandeExemp);
        } else {
            throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
        return demandeExemp;
    }

    /**
     * Méthode permettant de changer l'état d'une demande vers l'état ciblé dans le processus global de l'application
     * @param etape   etape à laquelle on souhaite retourner
     * @param demande demande que l'on souhaite modifier
     * @return demande modifiée
     * @throws DemandeCheckingException : demande dans un etat incorrect
     */
    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        switch (etape) {
            case 2:
                //retour de la demande d'exemplarisation au stade choix du type, à l'état en saisie (=preparation)
                demandeExemp.setTypeExemp(null); //effacement type d'exemplarisation obtenu a : ETAPE2
                demandeExemp.setIndexRecherche(null); //effacement de l'index de recherche obtenu a etape chargement du fichier : ETAPE3
                //le commentaire n'est pas effacé, il est géré dans le tableau de bord : pas dans les ETAPES
                demandeExemp.setListeZones(null); //effacement de la liste des zones obtenu a etape chargement du fichier : ETAPE3
                demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_PREPARATION)); //retour en ETAT 1
                /*suppression des lignes de la table LIGNE_FICHIER_EXEMP crées à chargement du fichier : ETAPE3
                On supprime les lignes qui ont en REF_DEMANDE l'id de la demande*/
                ligneFichierExempDao.deleteLigneFichierExempByDemandeExempId(demandeExemp.getId());
                //Mise à jour de l'entité
                save(demandeExemp);
                //Suppression du fichier sur disque non nécessaire, sera écrasé au prochain upload
                //Retour de la demande
                return demandeExemp;
            case 3:
                demandeExemp.setIndexRecherche(null);
                demandeExemp.setListeZones(null);
                demandeExemp.setEtatDemande(new EtatDemande(Constant.ETATDEM_ACOMPLETER)); //retour en ETAT3
                ligneFichierExempDao.deleteLigneFichierExempByDemandeExempId(demandeExemp.getId());
                save(demandeExemp);
                return demandeExemp;
            default:
                throw new DemandeCheckingException(Constant.GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED);
        }
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return changeState(demande, Constant.ETATDEM_TERMINEE);
    }

    /**
     * Méthode permettant d'interroger le Sudoc, de créer un exemplaire et de le retourner pour la simulation
     *
     * @param demandeExemp      demande d'exemplarisation concernée
     * @param ligneFichierExemp ligneFichier à traiter
     * @return la chaine de l'exemplaire construit, ou message d'erreur
     */
    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demandeExemp, LigneFichier ligneFichierExemp) throws CBSException, IOException, ZoneException {
        try {
            DemandeExemp demande = (DemandeExemp) demandeExemp;
            LigneFichierExemp ligneFichier = (LigneFichierExemp) ligneFichierExemp;
            traitementService.authenticate("M" + demande.getRcr());
            String numEx = launchQueryToSudoc(demande, ligneFichier.getIndexRecherche());
            //Retourne le tableau exemplaires existants / Exemplaire à créer
            return new String[]{
                    //L'indice 0 retourne le PPN de la notice
                    traitementService.getCbs().getPpnEncours(),
                    //L'indice 1 retourne les données locales et les exemplaires existants tous ensemble sous forme d'une chaine
                    Utilitaires.removeNonPrintableCharacters(donneeLocaleExistante).replace("\r", "\r\n") + "\r\n" + exemplairesExistants.replace("\r", "\r\n"), //2*r\n\ comptent pour un saut de ligne
                    //L'indice 2 retourne le bloc de données locales et l'exemplaire à créer
                    creerDonneesLocalesFromHeaderEtValeur(demande.getListeZones(), ligneFichier.getValeurZone()).replace("\r", "\r\n") + "\r\n" +
                            creerExemplaireFromHeaderEtValeur(demande.getListeZones(), ligneFichier.getValeurZone(), demande.getRcr(), numEx).replace("\r", "\r\n"),
            };
            //Si l'utilisateur n'a pas autorisé la création d'exemplaires multiples sur les notices de cette demande associée à ce RCR en cas d'exemplaires déjà présents

        } catch (QueryToSudocException e) {
            throw new CBSException(Level.ERROR, e.getMessage());
        } finally {
            traitementService.disconnect();
        }
    }

    /**
     * @param demande : la demande à partir de laquelle on va construire la requête
     * @param valeurs : tableau des valeurs des index de recherche
     * @return le numéro du prochain exemplaire à créer dans la notice au format "xx"
     */
    public String launchQueryToSudoc(DemandeExemp demande, String valeurs) throws CBSException, QueryToSudocException, IOException {
        String[] tabvaleurs = valeurs.split(";");
        String query = getQueryToSudoc(demande.getIndexRecherche().getCode(), demande.getTypeExemp().getNumTypeExemp(), tabvaleurs);

        if (!query.isEmpty()) {
            try {
                traitementService.getCbs().search(query);
                nbReponses = traitementService.getCbs().getNbNotices();
            } catch (IOException e) {
                nbReponses = 0;
            }
            switch (nbReponses) {
                //Le sudoc n'a pas trouvé de notice correspondant au PPN ou autre critère de recherche
                case 0:
                    throw new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND);
                case 1:
                    //Le sudoc à trouvé une notice correspondant au critère
                    String notice = traitementService.getCbs().getClientCBS().mod("1", String.valueOf(traitementService.getCbs().getLotEncours()));
                    String numExStr = Utilitaires.getLastNumExempFromNotice(notice);
                    //On controle ici pour la notice trouvée dans le sudoc le nombre d'exemplaires déjà présents sur ce RCR
                    donneeLocaleExistante = Utilitaires.getDonneeLocaleExistante(notice);
                    exemplairesExistants = Utilitaires.getExemplairesExistants(notice);
                    int numEx = Integer.parseInt(numExStr);
                    numEx++;
                    return (numEx < 10) ? "0" + numEx : Integer.toString(numEx); //On retourne le numero d'exemplaire ou sera enregistré le nouvel exemplaire
                default:
                    throw new QueryToSudocException(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + traitementService.getCbs().getListePpn());
            }
        } else {
            throw new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_NOT_COMPLIANT);
        }
    }

    /**
     * Récupération de la prochaine demande en attente à traiter
     *
     * @return demande récupérée dans la base
     */
    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        List<DemandeExemp> listeDemandes;
        if (currentHour >= minHour && currentHour < maxHour) {
            listeDemandes = demandeExempDao.getNextDemandeToProceedWithoutDAT();
        } else {
            listeDemandes = demandeExempDao.getNextDemandeToProceed();
        }
        if (!listeDemandes.isEmpty())
            return listeDemandes.get(0);
        return null;
    }

    /**
     * Récupération de la prochaine demande terminée à archiver
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeExemp> getDemandesToArchive() {
        List<DemandeExemp> listeDemandes = demandeExempDao.getNextDemandeToArchive();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande archivée à placer en statut supprimé
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeExemp> getDemandesToPlaceInDeletedStatus() {
        List<DemandeExemp> listeDemandes = demandeExempDao.getNextDemandeToPlaceInDeletedStatus();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    /**
     * Récupération de la prochaine demande en statut supprimé à supprimer définitivement
     *
     * @return demande récupérée dans la base
     */
    @Override
    public List<DemandeExemp> getDemandesToDelete() {
        List<DemandeExemp> listeDemandes = demandeExempDao.getNextDemandeToDelete();
        if (!listeDemandes.isEmpty())
            return listeDemandes;
        return null;
    }

    @Override
    public DemandeExemp creerDemande(String rcr, Integer userNum) {
        Calendar calendar = Calendar.getInstance();
        DemandeExemp demandeExemp = new DemandeExemp(rcr, calendar.getTime(), calendar.getTime(), referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), null, utilisateurService.findById(userNum));
        demandeExemp.setIln(Objects.requireNonNull(libProfileDao.findById(rcr).orElse(null)).getIln());
        setIlnShortNameOnDemande(demandeExemp);
        DemandeExemp demToReturn = (DemandeExemp) save(demandeExemp);
        journalService.addEntreeJournal(demandeExemp, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION));
        return demToReturn;
    }

    /**
     * Méthode de construction d'un exemplaire à partir de l'en tête du fichier et des valeurs associées
     *
     * @param header      : chaine contenant la liste des zones à créer (séparées par des ;)
     * @param valeurZones : chaine contenant les valeurs des zones à créer (séparées par des ;)
     * @return l'exemplaire sous forme de chaine
     */
    public String creerExemplaireFromHeaderEtValeur(String header, String valeurZones, String rcr, String numExemp) throws ZoneException {
        String[] listeHeader = header.split(";");
        String[] listeValeur = valeurZones.split(";");
        String zonePrecedente = "";
        //variable permettant de déterminer si une 930 a été ajoutée dans l'exemplaire
        boolean added930 = false;
        boolean zonePrecedenteVide = false;
        //Création d'un exemplaire vide
        Exemplaire exemp = new Exemplaire();
        //le fichier ayant été vérifié les deux tableaux ont la même taille
        for (int i = 0; i < listeHeader.length; i++) {
            String headerEnCours = listeHeader[i]; //entête en cours (une zone, une zone avec sous zone, ou une sous zone)
            String valeurEnCours = listeValeur[i]; //valeur en cours, associée à l'entête juste avant
            if (!Utilitaires.isDonneeLocale(listeHeader[i], zonePrecedente)) {
                Pattern patternHeader = Pattern.compile(Constant.REG_EXP_ZONE_SOUS_ZONE);
                Matcher matcher = patternHeader.matcher(headerEnCours);
                if (matcher.find()) {
                    //cas où on ajoute une zone + sous zone
                    String labelZone = matcher.group("zone");
                    if (labelZone.equals("930") && !("").equals(valeurEnCours)) {
                        added930 = true;
                    }
                    if (Utilitaires.isEtatCollection(labelZone)) {
                        //cas d'une zone d'état de collection
                        if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                            exemp.addZoneEtatCollection(labelZone, matcher.group("sousZone"), valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(labelZone).toCharArray());
                        } else {
                            zonePrecedenteVide = true;
                        }
                    } else {
                        //cas ou le header en cours est une zone + indicateur + sous zone classique
                        if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                            exemp.addZone(labelZone, matcher.group("sousZone"), valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(labelZone).toCharArray());
                        } else {
                            zonePrecedenteVide = true;
                        }
                    }
                    zonePrecedente = matcher.group("zone");
                } else {
                    //cas où on ajoute une sous zone à une zone déjà insérée
                    if (zonePrecedenteVide) {
                        if (Utilitaires.isEtatCollection(zonePrecedente)) {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                exemp.addZoneEtatCollection(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                            }
                        } else {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                if (!added930)
                                    exemp.addZone(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                                else
                                    exemp.addSousZone(zonePrecedente, headerEnCours, valeurEnCours);
                                if (zonePrecedente.equals("930")) {
                                    added930 = true;
                                }
                            }
                        }
                        zonePrecedenteVide = false;
                    } else {
                        if (Utilitaires.isEtatCollection(zonePrecedente)) {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                List<Zone> listezone = exemp.findZones(zonePrecedente);
                                if (!listezone.isEmpty()) {
                                    ZoneEtatColl zone = (ZoneEtatColl) listezone.get(0);
                                    zone.addSousZone(headerEnCours, valeurEnCours, 0);
                                } else {
                                    if (headerEnCours.equals("$4")) {
                                        //cas où on essaie d'ajouter une $4 seule dans la 955
                                        exemp.addZoneEtatCollection(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                                        ZoneEtatColl zone = (ZoneEtatColl) exemp.findZones(zonePrecedente).get(0);
                                        zone.addSousZone(headerEnCours, valeurEnCours, 0);
                                    }
                                }
                            }
                        } else {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                //cas ou le header en cours est une sous zone seule
                                if (!exemp.findZones(zonePrecedente).isEmpty()) {
                                    exemp.addSousZone(zonePrecedente, headerEnCours, valeurEnCours);
                                } else {
                                    exemp.addZone(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                                    if (zonePrecedente.equals("930")) {
                                        added930 = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (added930) {
            //une 930 a été rajoutée, on rajoute juste une sous zone $b
            exemp.addSousZone("930", "$b", rcr);
        } else {
            //pas de 930 ajoutée par l'utilisateur, on la crée avec une $b
            exemp.addZone("930", "$b", rcr, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone("930").toCharArray());
        }
        //ajout de la exx
        exemp.addZone("e" + numExemp, "$b", "x");
        //ajout de la 991 $a
        ajout991(exemp);
        return exemp.toString();
    }


    /**
     * Méthode d'ajout d'une zone 991 prédéfinie dans l'exemplaire
     *
     * @param exemp : exemplaire sur lequel rajouter la 991
     */
    private void ajout991(Exemplaire exemp) throws ZoneException {
        String datePattern = "dd-MM-yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        String date = simpleDateFormat.format(Calendar.getInstance().getTime());
        String valeur991 = Constant.TEXTE_991_CREA + " le " + date;
        char[] indicateurs = {'#', '#'};
        exemp.addZone("991", "$a", valeur991, indicateurs);
    }

    /**
     * Méthode de création des données locales à partir de l'en tête du fichier et des valeurs associées
     *
     * @param header : header du fichier
     * @param valeurZones : valeurs des zones à ajouter dans les données locales
     * @return les données locales de la notice
     */
    public String creerDonneesLocalesFromHeaderEtValeur(String header, String valeurZones) throws ZoneException {
        String[] listeHeader = header.split(";");
        String[] listeValeur = valeurZones.split(";");
        String zonePrecedente = "";
        DonneeLocale donneeLocale = new DonneeLocale(Constants.STR_1F + this.donneeLocaleExistante + Constants.STR_1E);
        for (int i = 0; i < listeHeader.length; i++) {
            if (Utilitaires.isDonneeLocale(listeHeader[i], zonePrecedente)) {
                String headerEnCours = listeHeader[i];
                String valeurEnCours = listeValeur[i];
                Pattern patternHeader = Pattern.compile(Constant.REG_EXP_DONNEELOCALE);
                Matcher matcher = patternHeader.matcher(headerEnCours);

                if (matcher.find()) {
                    donneeLocale.addZone(matcher.group("zone"), matcher.group("sousZone"), valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(matcher.group("zone")).toCharArray());
                    zonePrecedente = matcher.group("zone");
                } else {
                    donneeLocale.addSousZone(zonePrecedente, headerEnCours, valeurEnCours);
                }
            }
        }

        //replaceAll -> retire les caractères ASCII Printables sur des plages définies
        return donneeLocale.toString();
    }


    /**
     * Méthode de génération de la première ligne d'en tête du fichier résultat
     *
     * @param demande demande concernée
     * @return la chaine correspondant à l'en tête du fichier de résultat
     */
    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return "Exemplarisation démarrée le;" + Constant.formatDate.format(dateDebut) + "\n"
                + "requête;nb réponses;liste PPN;Correspondance L035;résultat;";
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        List<DemandeExemp> demandeExemp = demandeExempDao.getAllArchivedDemandesExemp(iln);
        List<Demande> demandeList = new ArrayList<>(demandeExemp);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        List<DemandeExemp> demandeExemp = demandeExempDao.getAllArchivedDemandesExempExtended();
        List<Demande> demandeList = new ArrayList<>(demandeExemp);
        setIlnShortNameOnList(demandeList);
        return demandeList;
    }


    /**
     * Méthode construisant la requête che en fonction des paramètres d'une demande d'exemplarisation
     *
     * @param codeIndex code de l'index de la recherche
     * @param valeur    tableau des valeurs utilisées pour construire la requête
     * @return requête che prête à être lancée vers le CBS
     */
    @Override
    public String getQueryToSudoc(String codeIndex, Integer typeExemp, String[] valeur) throws QueryToSudocException {
        switch (typeExemp) {
            case Constant.TYPEEXEMP_MONOELEC:
                switch (codeIndex) {
                    case "ISBN":
                        return "tno t; tdo o; che isb " + valeur[0];
                    case "PPN":
                        return "che ppn " + valeur[0];
                    case "SOU":
                        return "tno t; tdo o; che sou " + valeur[0];
                }
            case Constant.TYPEEXEMP_PERIO:
                switch (codeIndex) {
                    case "ISSN":
                        return "tno t; tdo t; che isn " + valeur[0];
                    case "PPN":
                        return "che ppn " + valeur[0];
                    case "SOU":
                        return "tno t; tdo t; che sou " + valeur[0];
                }
            case Constant.TYPEEXEMP_AUTRE:
                switch (codeIndex) {
                    case "ISBN":
                        return "tno t; tdo b; che isb " + valeur[0];
                    case "PPN":
                        return "che ppn " + valeur[0];
                    case "SOU":
                        return "tno t; tdo b; che sou " + valeur[0];
                    case "DAT":
                        if (valeur[1].isEmpty()) {
                            return "tno t; tdo b; apu " + valeur[0] + "; che mti " + Utilitaires.replaceDiacritical(valeur[2]);
                        }
                        return "tno t; tdo b; apu " + valeur[0] + "; che aut " + Utilitaires.replaceDiacritical(valeur[1]) + " et mti " + Utilitaires.replaceDiacritical(valeur[2]);
                }
            default:
                throw new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_NOT_RECOGNIZED_FOR_DEMANDE);
        }
    }

    /**
     * méthode d'archivage d'une demande
     * supprime les lignes fichiers au moment de l'archivage
     *
     * @param demande demande à archiver
     * @return la demande dans l'état archivé
     * @throws DemandeCheckingException : erreur de vérification de la demande
     */
    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        ligneFichierService.deleteByDemande(demandeExemp);
        return changeState(demandeExemp, Constant.ETATDEM_ARCHIVEE);
    }

    @Override
    public void modifierShortNameDemande(Demande demande) {
        setIlnShortNameOnDemande(demande);
    }
}
