package ubb.bmad.logsentinel.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @Test
    void shouldParseGeoIpDatabasePath() {
        ConfigLoader config = new ConfigLoader();
        config.load(new String[]{"-geo", "custom.mmdb"});
        assertEquals("custom.mmdb", config.getGeoIpDatabasePath());
    }

    @Test
    void shouldHaveDefaultGeoIpDatabasePath() {
        ConfigLoader config = new ConfigLoader();
        config.load(new String[]{});
        assertEquals("GeoLite2-City.mmdb", config.getGeoIpDatabasePath());
    }

    @Test
    void shouldParseAllArguments() {
        ConfigLoader config = new ConfigLoader();
        config.load(new String[]{"-f", "test.log", "-t", "10", "-w", "5", "-o", "out.txt", "-geo", "test.mmdb", "-live"});
        
        assertEquals("test.log", config.getLogFile());
        assertEquals(10, config.getThreshold());
        assertEquals(5, config.getWindowMinutes());
        assertEquals("out.txt", config.getReportOutput());
        assertEquals("test.mmdb", config.getGeoIpDatabasePath());
        assertTrue(config.isLiveMode());
    }

    @Test
    void shouldHandleInvalidThreshold() {
        ConfigLoader config = new ConfigLoader();
        // Should ignore invalid format and keep default (5)
        config.load(new String[]{"-t", "invalid"});
        assertEquals(5, config.getThreshold());
    }

    @Test
    void shouldHandleMissingValueForFlag() {
        ConfigLoader config = new ConfigLoader();
        config.load(new String[]{"-f"});
        assertEquals("sample_server.log", config.getLogFile());
    }
}
