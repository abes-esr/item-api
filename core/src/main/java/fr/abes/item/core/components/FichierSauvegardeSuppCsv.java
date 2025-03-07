package fr.abes.item.core.components;

import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.StorageException;
import fr.abes.item.core.service.ReferenceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Setter
@Getter
@Component
@Slf4j
public class FichierSauvegardeSuppCsv extends AbstractFichier implements Fichier {

    private final ReferenceService referenceService;

    public FichierSauvegardeSuppCsv(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    public void writePpnInFile(String ppn, Exemplaire exemplaire, String typeDoc) throws StorageException {
        try (FileWriter fw = new FileWriter(this.getPath().resolve(this.getFilename()).toString(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // création de la liste de référence pour trouver l'emplacement de chaque zone et sous-zone
            List<String> listDeReference = referenceService.constructHeaderCsv();
            listDeReference.subList(0, 4).clear();
            // ajout de la ligne
            String rcr = exemplaire.findZone("930", 0).findSubLabel("$b");
            String epn = exemplaire.findZone("A99", 0).getValeur();
            out.println(typeDoc + ";" + ppn + ";" + rcr + ";" + epn + ";" + gererZones(listDeReference, exemplaire));
        } catch (IOException ex) {
            throw new StorageException("Impossible d'écrire dans le fichier de sauvegarde csv");
        }
    }

    public String gererZones(List<String> listeZonesEtSousZones, Exemplaire exemplaire) {
        return gererZonesRecursif(listeZonesEtSousZones, 0, exemplaire, "", null);
    }

    private String gererZonesRecursif(List<String> listeZonesEtSousZones, int index, Exemplaire exemplaire, String resultat, Zone zone) {
        if (index >= listeZonesEtSousZones.size()) {
            if (resultat != null && !resultat.isEmpty()) {
                return resultat.substring(0, resultat.length()-1);
            }
            return null;
        }
        String zoneSousZone = listeZonesEtSousZones.get(index);

        if (zoneSousZone.startsWith("$")) {
            if (zone != null) {
                String sousZone = zone.findSubLabel(zoneSousZone);
                if (sousZone != null) {
                    resultat += sousZone;
                }
            }
        } else {
            zone = exemplaire.findZone(zoneSousZone.split("\\$")[0],0);
            if (zone != null) {
                String sousZone = zone.findSubLabel(zoneSousZone.split("\\$")[1]);
                if (sousZone != null) {
                    resultat += sousZone;
                }
            }
        }
        return gererZonesRecursif(listeZonesEtSousZones, index+1, exemplaire, resultat + ";", zone);
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_ATTENTE_2;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_SAUVEGARDE_NAME + demande.getId() + Constant.EXTENSIONCSV;
    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {
        //non implémentée
    }

    public void writeHeader() {
        if (!isHeaderExist()) {
            try (FileWriter fw = new FileWriter(this.getPath().resolve(this.getFilename()).toString(), true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                // ajout de la ligne
                out.println(String.join(";", this.referenceService.constructHeaderCsv()));
            } catch (IOException ex) {
                throw new StorageException(Constant.ERR_FILE_WRITING + "de sauvegarde csv");
            }
        }
    }

    private boolean isHeaderExist() {
        File file = new File(this.getPath().resolve(this.getFilename()).toString());
        if(file.exists()) {
            try (FileReader fr = new FileReader(this.getPath().resolve(this.getFilename()).toString());
                 BufferedReader br = new BufferedReader(fr)) {
                String line = br.readLine();
                return line != null && line.startsWith("TYPE (008);PPN;");
            } catch (IOException ex) {
                throw new StorageException(Constant.ERR_FILE_READING + " de sauvegarde csv");
            }
        } else return false;
    }
}
