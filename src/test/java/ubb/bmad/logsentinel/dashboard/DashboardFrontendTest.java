package ubb.bmad.logsentinel.dashboard;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class DashboardFrontendTest {

    @Test
    void shouldHaveDashboardIndexInClasspath() {
        InputStream is = getClass().getResourceAsStream("/static/index.html");
        assertNotNull(is, "index.html should be in src/main/resources/static/");
        
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String content = s.hasNext() ? s.next() : "";
        
        assertTrue(content.contains("LogSentinel Dashboard"), "Should contain title");
        assertTrue(content.contains("id=\"stat-lines\""), "Should contain metrics containers");
        assertTrue(content.contains("id=\"incident-feed\""), "Should contain feed container");
        assertTrue(content.contains("new EventSource('/api/alerts')"), "Should contain SSE logic");
    }
}
