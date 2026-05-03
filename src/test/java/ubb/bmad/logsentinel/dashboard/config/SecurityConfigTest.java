package ubb.bmad.logsentinel.dashboard.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldDenyUnauthenticatedAccessToStats() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedAccessWithCorrectCredentials() throws Exception {
        mockMvc.perform(get("/api/stats").with(httpBasic("admin", "sentinel")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldAllowAuthenticatedAccessWithMockUser() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk());
    }
}
