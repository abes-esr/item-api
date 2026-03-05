package fr.abes.item.web.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.item.core.entities.item.Utilisateur;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.dto.DtoBuilder;
import fr.abes.item.exception.RestResponseEntityExceptionHandler;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.web.UtilisateurRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {UtilisateurRestService.class, DtoBuilder.class, ObjectMapper.class})
public class UtilisateurRestServiceTest {
    @Autowired
    WebApplicationContext context;
    @InjectMocks
    UtilisateurRestService controller;
    @MockitoBean
    UtilisateurService service;
    @MockitoBean
    CheckAccessToServices checkAccessToServices;
    @Autowired
    DtoBuilder builder;
    @Autowired
    ObjectMapper mapper;
    MockMvc mockMvc;
    private static final Validator NO_OP_VALIDATOR = new Validator() {
        @Override
        public boolean supports(Class<?> clazz) {
            return true;
        }

        @Override
        public void validate(Object target, Errors errors) {
            // No-op validator for controller tests in offline env
        }
    };

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(context.getBean(UtilisateurRestService.class))
                .setControllerAdvice(new RestResponseEntityExceptionHandler())
                .setValidator(NO_OP_VALIDATOR)
                .build();
    }

    @Test
    void testCreationUtilisateur() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserMajUtilisateurParUserNum(1, "1");
        Utilisateur utilisateur = new Utilisateur(1, "", "1");
        Mockito.when(service.findById(1)).thenReturn(utilisateur);
        Utilisateur utilisateurSaved = new Utilisateur(1, "test@test.com", "1");
        Mockito.when(service.save(Mockito.any())).thenReturn(utilisateurSaved);

        this.mockMvc.perform(post("/api/v1/utilisateurs/1").content("test@test.com").requestAttr("userNum", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void testPatchUtilisateur() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserMajUtilisateurParUserNum(1, "1");
        Utilisateur utilisateur = new Utilisateur(1, "truc@truc.Com", "1");
        Mockito.when(service.findById(1)).thenReturn(utilisateur);
        Utilisateur utilisateurSaved = new Utilisateur(1, "test@test.com", "1");
        Mockito.when(service.save(Mockito.any())).thenReturn(utilisateurSaved);

        this.mockMvc.perform(patch("/api/v1/utilisateurs/1").content("test@test.com").requestAttr("userNum", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void testPatchUtilisateurUnknownUser() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserMajUtilisateurParUserNum(1, "1");
        Mockito.when(service.findById(1)).thenReturn(null);
        Utilisateur utilisateurSaved = new Utilisateur(1, "test@test.com", "1");
        Mockito.when(service.save(Mockito.any())).thenReturn(utilisateurSaved);

        this.mockMvc.perform(patch("/api/v1/utilisateurs/1").content("test@test.com").requestAttr("userNum", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Utilisateur inexistant"));
    }
}

