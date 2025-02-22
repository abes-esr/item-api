package fr.abes.item.web.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.service.impl.DemandeExempService;
import fr.abes.item.core.service.impl.DemandeModifService;
import fr.abes.item.core.service.impl.DemandeSuppService;
import fr.abes.item.core.service.impl.LigneFichierExempService;
import fr.abes.item.dto.DtoBuilder;
import fr.abes.item.exception.RestResponseEntityExceptionHandler;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.web.DemandeRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {DemandeRestService.class, StrategyFactory.class, DtoBuilder.class, ObjectMapper.class})
class DemandeRestServiceTest {
    @Autowired
    WebApplicationContext context;
    @InjectMocks
    DemandeRestService controller;
    @MockBean
    DemandeExempService demandeExempService;
    @MockBean
    DemandeModifService demandeModifService;
    @MockBean
    DemandeSuppService demandeSuppService;
    @MockBean
    LigneFichierExempService ligneFichierExempService;
    @MockBean
    CheckAccessToServices checkAccessToServices;
    @Autowired
    StrategyFactory strategy;
    @Autowired
    DtoBuilder builder;
    @Autowired
    ObjectMapper mapper;

    List<DemandeDto> demandeDto = new ArrayList<>();

    MockMvc mockMvc;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(context.getBean(DemandeRestService.class)).setControllerAdvice(new RestResponseEntityExceptionHandler()).build();
        Calendar cal = Calendar.getInstance();
        DemandeExemp demande1 = new DemandeExemp(1);
        cal.set(2024, Calendar.APRIL, 15);
        demande1.setDateCreation(cal.getTime());
        cal.set(2024, Calendar.MARCH, 20);
        demande1.setDateModification(cal.getTime());
        demande1.setRcr("111111111");
        demande1.setIndexRecherche(new IndexRecherche(1, "PPN"));
        DemandeExemp demande2 = new DemandeExemp(2);
        demande2.setRcr("222222222");
        demande2.setTypeExemp(new TypeExemp(1, "Monographies"));
        cal.set(2024, Calendar.APRIL, 15);
        demande2.setDateCreation(cal.getTime());
        cal.set(2024, Calendar.MARCH, 20);
        demande2.setDateModification(cal.getTime());

        DemandeDto demandeDto1 = new DemandeDto(demande1, 1);
        DemandeDto demandeDto2 = new DemandeDto(demande2, 2);
        demandeDto.addAll(Lists.newArrayList(demandeDto1, demandeDto2));

    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetAllActiveDemandesForAdmin() throws Exception {
        Mockito.when(demandeExempService.getAllActiveDemandesForAdminExtended()).thenReturn(this.demandeDto);
        this.mockMvc.perform(get("/api/v1/demandes/EXEMP/?archive=false&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[0].indexRecherche").value("PPN"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"))
                .andExpect(jsonPath("$[1].typeExemp").value("Monographies"));

    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetAllActiveDemandesForAdminExtender() throws Exception {
        Mockito.when(demandeExempService.getAllActiveDemandesForAdmin("1")).thenReturn(this.demandeDto);
        this.mockMvc.perform(get("/api/v1/demandes/EXEMP/?archive=false&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testChercher() throws Exception {
        Mockito.when(demandeExempService.getActiveDemandesForUser("1")).thenReturn(this.demandeDto);
        this.mockMvc.perform(get("/api/v1/demandes/EXEMP/?archive=false&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetAllArtiveDemandes() throws Exception {
        Mockito.when(demandeExempService.getActiveDemandesForUser("1")).thenReturn(this.demandeDto);
        this.mockMvc.perform(get("/api/v1/demandes/EXEMP?archive=false&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetAllArchivedDemandes() throws Exception {
        Mockito.when(demandeExempService.getAllArchivedDemandes("1")).thenReturn(this.demandeDto);
        this.mockMvc.perform(get("/api/v1/demandes/EXEMP?archive=true&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetDemande() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.findById(1)).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        this.mockMvc.perform(get("/api/v1/demandes/EXEMP/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("111111111"))
                .andExpect(jsonPath("$.indexRecherche").value("PPN"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testCreerDemande() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.creerDemande(Mockito.anyString(), Mockito.anyInt())).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        this.mockMvc.perform(post("/api/v1/demandes/EXEMP?rcr=341720001").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("111111111"));

    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testModifDemandeRcr() throws Exception {
        Calendar cal = Calendar.getInstance();
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.creerDemande(Mockito.anyString(), Mockito.anyInt())).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        EtatDemande etat = new EtatDemande(1, "A compléter");
        Utilisateur utilisateur =  new Utilisateur(1, "test@test.com");
        DemandeExemp demandeIn = new DemandeExemp(1, "341720001", cal.getTime(), cal.getTime(), etat, "", utilisateur);
        Mockito.when(demandeExempService.findById(1)).thenReturn(demandeIn);
        DemandeExemp demandeOut = new DemandeExemp(1, "341725201", cal.getTime(), cal.getTime(), etat, "", utilisateur);
        Mockito.when(demandeExempService.save(Mockito.any())).thenReturn(demandeOut);
        this.mockMvc.perform(patch("/api/v1/demandes/EXEMP/1?rcr=341725201").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("341725201"))
                .andExpect(jsonPath("$.etatDemande").value("A compléter"));

    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testModifDemandeTypeExemp() throws Exception {
        Calendar cal = Calendar.getInstance();
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.creerDemande(Mockito.anyString(), Mockito.anyInt())).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        EtatDemande etat = new EtatDemande(1, "A compléter");
        Utilisateur utilisateur =  new Utilisateur(1, "test@test.com");
        DemandeExemp demandeIn = new DemandeExemp(1, "341720001", cal.getTime(), cal.getTime(), etat, "", utilisateur);
        Mockito.when(demandeExempService.findById(1)).thenReturn(demandeIn);
        DemandeExemp demandeOut = new DemandeExemp(1, "341725201", cal.getTime(), cal.getTime(), etat, "", utilisateur);
        demandeOut.setTypeExemp(new TypeExemp(1, "Monographies Electroniques"));
        Mockito.when(demandeExempService.majTypeExemp(1, 1)).thenReturn(demandeOut);
        var result = this.mockMvc.perform(patch("/api/v1/demandes/EXEMP/1?typeExemp=1").requestAttr("userNum", "1"));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("341725201"))
                .andExpect(jsonPath("$.etatDemande").value("A compléter"))
                .andExpect(jsonPath("$.typeExemp").value("Monographies Electroniques"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testModifDemandeTypeSuppression() throws Exception {
        Calendar cal = Calendar.getInstance();
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.SUPP);
        EtatDemande etat = new EtatDemande(1, "A compléter");
        Utilisateur utilisateur =  new Utilisateur(1, "test@test.com");
        DemandeSupp demandeIn = new DemandeSupp(1, "341725201", cal.getTime(), cal.getTime(), "", etat, utilisateur);
        Mockito.when(demandeSuppService.findById(1)).thenReturn(demandeIn);
        demandeIn.setTypeSuppression(TYPE_SUPPRESSION.EPN);
        Mockito.when(demandeSuppService.majTypeSupp(1, TYPE_SUPPRESSION.EPN)).thenReturn(demandeIn);
        this.mockMvc.perform(patch("/api/v1/demandes/SUPP/1?typeSupp=EPN").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.typeSuppression").value("EPN"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testModifDemandeCommentaire() throws Exception {
        Calendar cal = Calendar.getInstance();
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.creerDemande(Mockito.anyString(), Mockito.anyInt())).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        EtatDemande etat = new EtatDemande(1, "A compléter");
        Utilisateur utilisateur =  new Utilisateur(1, "test@test.com");
        DemandeExemp demandeIn = new DemandeExemp(1, "341720001", cal.getTime(), cal.getTime(), etat, "", utilisateur);
        Mockito.when(demandeExempService.findById(1)).thenReturn(demandeIn);
        DemandeExemp demandeOut = new DemandeExemp(1, "341720001", cal.getTime(), cal.getTime(), etat, "", utilisateur);
        demandeOut.setCommentaire("commentaire test");
        Mockito.when(demandeExempService.save(Mockito.any())).thenReturn(demandeOut);
        var result = this.mockMvc.perform(patch("/api/v1/demandes/EXEMP/1?commentaire=commentaire test").requestAttr("userNum", "1"));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("341720001"))
                .andExpect(jsonPath("$.etatDemande").value("A compléter"))
                .andExpect(jsonPath("$.commentaire").value("commentaire test"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testModifDemandeTraitement() throws Exception {
        Calendar cal = Calendar.getInstance();
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.MODIF);
        Mockito.when(demandeModifService.creerDemande(Mockito.anyString(), Mockito.anyInt())).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        EtatDemande etat = new EtatDemande(1, "A compléter");
        Utilisateur utilisateur =  new Utilisateur(1, "test@test.com");
        Traitement traitement = new Traitement(1, "Créer nouvelle zone", "creerNouvelleZone");
        DemandeModif demandeIn = new DemandeModif(1, "341720001", cal.getTime(), cal.getTime(), "930", "$j", "", etat, utilisateur, traitement);
        Mockito.when(demandeModifService.findById(1)).thenReturn(demandeIn);
        DemandeModif demandeOut = new DemandeModif(1, "341720001", cal.getTime(), cal.getTime(), "930", "$j", "", etat, utilisateur, traitement);
        demandeOut.setTraitement(new Traitement(2, "Supprimer zone", "supprimerZone"));
        Mockito.when(demandeModifService.majTraitement(1, 2)).thenReturn(demandeOut);
        var result = this.mockMvc.perform(patch("/api/v1/demandes/MODIF/1?traitement=2").requestAttr("userNum", "1"));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("341720001"))
                .andExpect(jsonPath("$.etatDemande").value("A compléter"))
                .andExpect(jsonPath("$.traitement").value("Supprimer zone"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testSupprimer() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.doNothing().when(demandeExempService).deleteById(Mockito.anyInt());
        this.mockMvc.perform(delete("/api/v1/demandes/EXEMP/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(authorities = {"USER"})
    void testUpload() throws Exception {
        Calendar cal = Calendar.getInstance();
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "data".getBytes());
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        DemandeExemp demande = new DemandeExemp(1, "341720001", cal.getTime(), cal.getTime(), new EtatDemande(1, "A compléter"), "", new Utilisateur(1, "test@test.com"));
        Mockito.when(demandeExempService.findById(1)).thenReturn(demande);
        Mockito.doNothing().when(demandeExempService).initFiles(Mockito.any());
        Mockito.doNothing().when(demandeExempService).stockerFichier(Mockito.any(), Mockito.any());
        this.mockMvc.perform(multipart("/api/v1/uploadDemande/EXEMP/1").file(file).requestAttr("userNum", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testSimulerLigne() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.findById(1)).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        Mockito.when(ligneFichierExempService.getLigneFichierbyDemandeEtPos(Mockito.any(), Mockito.anyInt())).thenReturn(new LigneFichierExemp());
        Mockito.when(ligneFichierExempService.getNoticeExemplaireAvantApres(Mockito.any(), Mockito.any())).thenReturn(new String[]{"avant", "après"});
        this.mockMvc.perform(get("/api/v1/simulerLigne/EXEMP/1/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0]").value("avant"))
                .andExpect(jsonPath("$.[1]").value("après"));
    }

    @Test
    void testPasserEnAttente() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.findById(1)).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        DemandeExemp demande = (DemandeExemp) this.demandeDto.get(0).getDemande();
        demande.setEtatDemande(new EtatDemande(3, "En attente"));
        Mockito.when(demandeExempService.changeState(Mockito.any(), Mockito.anyInt())).thenReturn(demande);
        this.mockMvc.perform(patch("/api/v1/passerEnAttente/EXEMP/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.etatDemande").value("En attente"))
                .andExpect(jsonPath("$.rcr").value("111111111"));
    }

    @Test
    void testArchiver() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        DemandeExemp demande = (DemandeExemp) this.demandeDto.get(0).getDemande();
        demande.setEtatDemande(new EtatDemande(7, "Archivée"));
        Mockito.when(demandeExempService.archiverDemande(Mockito.any())).thenReturn(demande);
        this.mockMvc.perform(get("/api/v1/archiverDemande/EXEMP/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.etatDemande").value("Archivée"))
                .andExpect(jsonPath("$.rcr").value("111111111"));
    }

    @Test
    void testPreviousStep() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        DemandeExemp demande = (DemandeExemp) this.demandeDto.get(0).getDemande();
        demande.setEtatDemande(new EtatDemande(3, "En attente"));
        Mockito.when(demandeExempService.previousState(Mockito.any())).thenReturn(demande);
        this.mockMvc.perform(patch("/api/v1/etapePrecedente/EXEMP/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.etatDemande").value("En attente"))
                .andExpect(jsonPath("$.rcr").value("111111111"));
    }

    @Test
    void testChosenStep() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        DemandeExemp demande = (DemandeExemp) this.demandeDto.get(0).getDemande();
        demande.setEtatDemande(new EtatDemande(3, "En attente"));
        Mockito.when(demandeExempService.returnState(Mockito.anyInt(), Mockito.any())).thenReturn(demande);
        this.mockMvc.perform(patch("/api/v1/etapeChoisie/EXEMP/1").param("etape", "3").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.etatDemande").value("En attente"))
                .andExpect(jsonPath("$.rcr").value("111111111"));
    }

    @Test
    void testGetNbLigneFichier() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1", TYPE_DEMANDE.EXEMP);
        Mockito.when(demandeExempService.findById(Mockito.anyInt())).thenReturn((DemandeExemp) this.demandeDto.get(0).getDemande());
        Mockito.when(ligneFichierExempService.getNbLigneFichierTotalByDemande(Mockito.any())).thenReturn(30);
        this.mockMvc.perform(get("/api/v1/nbLignesFichier/EXEMP/1").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(30));
    }
}
