package ubb.bmad.logsentinel.dashboard.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ubb.bmad.logsentinel.dashboard.AppState;
import ubb.bmad.logsentinel.engine.Incident;
import ubb.bmad.logsentinel.engine.IncidentRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StatsControllerTest {

    private MockMvc mockMvc;
    private IncidentRepository mockRepository;
    private AppState mockAppState;

    @BeforeEach
    void setUp() {
        mockRepository = mock(IncidentRepository.class);
        mockAppState = mock(AppState.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new StatsController(mockRepository, mockAppState)).build();
    }

    @Test
    void shouldReturnInitialStats() throws Exception {
        when(mockAppState.getTotalLinesProcessed()).thenReturn(0);
        when(mockAppState.getUniqueIpsScanned()).thenReturn(0);
        when(mockAppState.getThreatCount()).thenReturn(0);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinesProcessed").value(0))
                .andExpect(jsonPath("$.uniqueIpsScanned").value(0))
                .andExpect(jsonPath("$.threatCount").value(0));
    }

    @Test
    void shouldReturnUpdatedStats() throws Exception {
        when(mockAppState.getTotalLinesProcessed()).thenReturn(1);
        when(mockAppState.getUniqueIpsScanned()).thenReturn(1);
        when(mockAppState.getThreatCount()).thenReturn(1);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinesProcessed").value(1))
                .andExpect(jsonPath("$.uniqueIpsScanned").value(1))
                .andExpect(jsonPath("$.threatCount").value(1));
    }

    @Test
    void shouldReturnHistoricalData() throws Exception {
        Incident incident = Incident.builder()
                .ipAddress("2.2.2.2")
                .hitCount(10)
                .location("Germany, Berlin")
                .build();
        
        when(mockRepository.findAll()).thenReturn(List.of(incident));

        mockMvc.perform(get("/api/incidents/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ipAddress").value("2.2.2.2"))
                .andExpect(jsonPath("$[0].hitCount").value(10))
                .andExpect(jsonPath("$[0].location").value("Germany, Berlin"));
    }
}
