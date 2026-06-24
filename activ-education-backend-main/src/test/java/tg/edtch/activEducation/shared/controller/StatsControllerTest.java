package tg.edtch.activEducation.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("deprecation")
class StatsControllerTest {

    private MockMvc mockMvc;
    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = mock(StatsService.class);
        StatsController controller = new StatsController(statsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getKPIs_shouldReturnCounts() throws Exception {
        when(statsService.getKPIs()).thenReturn(Map.of(
                "totalEleves", 100L,
                "totalConseillers", 10L,
                "totalQuiz", 5L,
                "totalResultats", 200L,
                "totalEtablissements", 50L));

        mockMvc.perform(get("/api/v1/admin/stats/kpi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEleves").value(100))
                .andExpect(jsonPath("$.totalConseillers").value(10))
                .andExpect(jsonPath("$.totalQuiz").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInscriptions_shouldReturnList() throws Exception {
        when(statsService.getInscriptionsParJour(30))
                .thenReturn(List.of(Map.of("date", "2026-06-01", "count", 5L)));

        mockMvc.perform(get("/api/v1/admin/stats/inscriptions?jours=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-06-01"))
                .andExpect(jsonPath("$[0].count").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizCompletes_shouldReturnList() throws Exception {
        when(statsService.getQuizCompletesParJour(30))
                .thenReturn(List.of(Map.of("date", "2026-06-01", "count", 3L)));

        mockMvc.perform(get("/api/v1/admin/stats/quiz-completes?jours=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].count").value(3));
    }
}