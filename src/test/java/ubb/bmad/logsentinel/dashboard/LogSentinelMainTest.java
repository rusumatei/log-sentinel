package ubb.bmad.logsentinel.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ubb.bmad.logsentinel.Main;

/**
 * Verifies that the unified Spring Boot context (Main.class) loads correctly.
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogSentinelMainTest {

    @Test
    void contextLoads() {
        // Verifies that the unified Main application context starts successfully
    }
}
