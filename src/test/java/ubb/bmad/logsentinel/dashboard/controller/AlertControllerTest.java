package ubb.bmad.logsentinel.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ubb.bmad.logsentinel.dashboard.service.AlertService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlertControllerTest {

    @Test
    void shouldSubscribeToAlerts() throws Exception {
        ubb.bmad.logsentinel.dashboard.AppState state = mock(ubb.bmad.logsentinel.dashboard.AppState.class);
        AlertService alertService = new AlertService(state);
        AlertController controller = new AlertController(alertService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        MvcResult result = mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("text/event-stream", result.getResponse().getContentType());
    }
}
