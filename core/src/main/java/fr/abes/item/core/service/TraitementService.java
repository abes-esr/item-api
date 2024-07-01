package fr.abes.item.core.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.cbs.utilitaire.Utilitaire;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.entities.item.Traitement;
import fr.abes.item.core.repository.item.ITraitementDao;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraitementService {
	private final ITraitementDao traitementDao;

	@Value("${sudoc.serveur}")
	private String serveurSudoc;

	@Value("${sudoc.port}")
	private String portSudoc;


	@Getter
	private ProcessCBS cbs;


	public TraitementService(ITraitementDao traitementDao) {
		this.traitementDao = traitementDao;
		cbs = new ProcessCBS();
    }

    public void authenticate(String login) throws CBSException, IOException {
        this.cbs = new ProcessCBS();
        this.cbs.authenticate(serveurSudoc, portSudoc, login, Constant.PASSSUDOC);
    }

    /**
     * Méthode de recherche d'un EPN et de récupération du premier exemplaire d'une notice
     *
     * @param epn : epn à rechercher
     * @return notice d'exemplaire trouvée
     * @throws CBSException : Erreur CBS
     * @throws IOException : erreur de communication avec le CBS
     */
    public String getNoticeFromEPN(String epn) throws CBSException, IOException {
        cbs.search("che EPN " + epn);
        if (cbs.getNbNotices() == 1) {
            String noticeEpn = cbs.getClientCBS().mod("1", String.valueOf(cbs.getLotEncours()));
            String numEx = Utilitaires.getNumExFromExemp(Utilitaires.getExempFromNotice(noticeEpn, epn));
            String resu = cbs.getClientCBS().modE(numEx, String.valueOf(cbs.getLotEncours()));
            cbs.back();
            String resu2 = Utilitaire.recupEntre(resu, Constants.VTXTE, Constants.STR_0D + Constants.STR_1E);
            return Constants.STR_1F + resu2.substring(resu2.indexOf("e" + numEx)) + Constants.STR_0D + Constants.STR_1E;
        } else {
            log.error(epn + " pas trouvé");
            throw new IOException(Constant.ERR_FILE_NOTICE_EPN_NUMBER);
        }
    }

    /**
     * Méthode permettant d'ajouter une zone / sous zone dans une notice d'exemplaire
     *
     * @param exemp notice à modifier
     * @return notice avec nouvelle zone / sous zone préfixée de STR_1F
     */
    public Exemplaire creerNouvelleZone(String exemp, String tag, String subTag, String valeur) throws ZoneException {
        Exemplaire exemplaire = new Exemplaire(exemp);
        exemplaire.addZone(tag, subTag, valeur);
        exemplaire = ajout991(exemplaire);
        return exemplaire;
    }

    /**
     * Méthode permettant la suppression d'une zone dans une notice d'exemplaire
     *
     * @param exemp notice biblio + exemplaires
     * @param tag   zone à supprimer
     * @return chaine de l'exemplaire modifié préfixé par STR_1F
     */
    public Exemplaire supprimerZone(String exemp, String tag) throws ZoneException {
        Exemplaire exemplaire = new Exemplaire(exemp);
        exemplaire.deleteZone(tag);
        exemplaire = ajout991(exemplaire);
        return exemplaire;
    }

    /**
     * Méthode permettant la suppression d'une sous-zone dans une notice d'exemplaire
     *
     * @param exemp  notice biblio + exemplaires
     * @param tag    zone qui contient la sous-zone
     * @param subTag zone à supprimer
     * @return chaine de l'exemplaire modifié préfixé par STR_1F
     */
    public Exemplaire supprimerSousZone(String exemp, String tag, String subTag) throws ZoneException {
        Exemplaire exemplaire = new Exemplaire(exemp);
        exemplaire.deleteSousZone(tag, subTag);
        exemplaire = ajout991(exemplaire);
        return exemplaire;
    }


    /**
     * Méthode permettant la création d'une sous-zone dans une notice d'exemplaire
     *
     * @param exemp  notice biblio + exemplaires
     * @param tag    zone qui contient la sous-zone
     * @param subTag sous-zone à créer
     * @param valeur valeur associée à la sous zone (la sous-zone est la clé)
     * @return l'exemplaire modifié
     */
    public Exemplaire creerSousZone(String exemp, String tag, String subTag, String valeur) throws ZoneException {
        Exemplaire exemplaire = new Exemplaire(exemp);
        exemplaire.addSousZone(tag, subTag, valeur);
        exemplaire = ajout991(exemplaire);
        return exemplaire;
    }

    /**
     * Méthode permettant le remplacement d'une sous-zone dans une notice d'exemplaire
     *
     * @param exemp  notice biblio + exemplaires
     * @param tag    zone qui contient la sous-zone
     * @param subTag sous-zone à remplacer
     * @param valeur valeur associée à la sous zone (la sous-zone est la clé)
     * @return l'exemplaire modifié
     */
    public Exemplaire remplacerSousZone(String exemp, String tag, String subTag, String valeur) throws ZoneException {
        Exemplaire exemplaire = new Exemplaire(exemp);
        try {
            exemplaire.replaceSousZone(tag, subTag, valeur);
            exemplaire = ajout991(exemplaire);
        } catch (NullPointerException ex) {
            log.debug("Zone / sous zone absente de la notice à modifier");
        }
        return exemplaire;
    }

    /**
     * Ajout d'une zone 991 $a indiquant la modification de la notice par le programme
     *
     * @param exemp exemplaire à modifier
     * @return exemplaire modifié
     */
    public Exemplaire ajout991(Exemplaire exemp) throws ZoneException {
        String datePattern = "dd-MM-yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        String date = simpleDateFormat.format(Calendar.getInstance().getTime());
        char[] indicateurs = new char[2];
        indicateurs[0] = '#';
        indicateurs[1] = '#';
        String valeurToInsert = Constant.TEXTE_991_MODIF + " le " + date;
        List<Zone> listZone = exemp.findZoneWithPattern("991", "$a", Constant.TEXTE_991_MODIF);
        if (listZone.isEmpty()) {
            exemp.addZone("991", "$a", valeurToInsert, indicateurs);
        } else {
            exemp.replaceSousZoneWithValue("991", "$a", Constant.TEXTE_991_MODIF, valeurToInsert);
        }
        return exemp;
    }

    /**
     * méthode de validation de la sauvegarde d'un exemplaire
     *
     * @param noticeModifiee notice à sauvegarder
     * @return : retour CBS
     * @throws CBSException : erreur CBS
     * @throws IOException : erreur de communication CBS
     */
    public String saveExemplaire(String noticeModifiee, String epn) throws CBSException, IOException {
        String numEx = Utilitaires.getNumExFromExemp(noticeModifiee);
        log.debug(epn + " sauvegarde exemplaire");
        return cbs.modifierExemp(noticeModifiee, numEx);
    }


    /**
     * Deconnexion du client CBS (sudoc)
     */
    public void disconnect() throws CBSException {
        cbs.getClientCBS().disconnect();
    }


    /**
     * Retourner l'ensemble de la liste des traitements disponibles
     *
     * @return liste de tous les traitements
     */
    public List<Traitement> findAll() {
        return traitementDao.findAllByOrderByNumTraitementAsc();
    }

    public Traitement findTraitementById(Integer id) {
        Optional<Traitement> traitement = traitementDao.findById(id);
        return traitement.orElseThrow();
    }

    public Integer findTraitementByDemandeId(Integer id) {
        return traitementDao.findTraitementByDemandeModifId(id);
    }


}
