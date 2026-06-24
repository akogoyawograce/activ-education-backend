package tg.edtch.activEducation.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;
import tg.edtch.activEducation.profil.domain.service.AdministrateurService;
import tg.edtch.activEducation.profil.domain.service.ConseillerService;
import tg.edtch.activEducation.profil.domain.service.EleveService;
import tg.edtch.activEducation.profil.domain.service.ParentService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("deprecation")
class TestControllerTest {

    private MockMvc mockMvc;
    private EleveService eleveService;
    private ParentService parentService;
    private ConseillerService conseillerService;
    private AdministrateurService adminService;

    @BeforeEach
    void setUp() {
        eleveService = mock(EleveService.class);
        parentService = mock(ParentService.class);
        conseillerService = mock(ConseillerService.class);
        adminService = mock(AdministrateurService.class);
        TestController controller = new TestController(eleveService, parentService, conseillerService, adminService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEleve_shouldReturn201() throws Exception {
        EleveResponse response = EleveResponse.builder()
                .trackingId(UUID.randomUUID())
                .email("test@test.com")
                .build();
        when(eleveService.inscrireEleve(any())).thenReturn(response);

        String body = """
                {
                    "typeUtilisateur": "ELEVE",
                    "email": "test@test.com",
                    "motDePasse": "password123",
                    "nom": "Test",
                    "prenom": "User"
                }
                """;

        mockMvc.perform(post("/api/v1/test/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn400ForInvalidType() throws Exception {
        String body = """
                {
                    "typeUtilisateur": "INVALID",
                    "email": "test@test.com",
                    "motDePasse": "password123",
                    "nom": "Test",
                    "prenom": "User"
                }
                """;

        mockMvc.perform(post("/api/v1/test/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createParent_shouldReturn201() throws Exception {
        tg.edtch.activEducation.profil.application.dto.response.ParentResponse parentResp =
                tg.edtch.activEducation.profil.application.dto.response.ParentResponse.builder()
                        .trackingId(UUID.randomUUID())
                        .email("parent@test.com")
                        .nom("Test")
                        .prenom("Parent")
                        .build();
        when(parentService.creerParent(any())).thenReturn(parentResp);

        String body = """
                {
                    "typeUtilisateur": "PARENT",
                    "email": "parent@test.com",
                    "motDePasse": "password123",
                    "nom": "Test",
                    "prenom": "Parent"
                }
                """;

        mockMvc.perform(post("/api/v1/test/create-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("parent@test.com"));
    }


}
