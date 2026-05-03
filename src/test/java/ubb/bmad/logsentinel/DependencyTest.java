package ubb.bmad.logsentinel;

import com.maxmind.geoip2.DatabaseReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DependencyTest {
    @Test
    void testMaxMindDependencyLoaded() {
        // We don't need a real file for this classpath check, 
        // just verify the class is accessible.
        Class<?> clazz = DatabaseReader.class;
        assertNotNull(clazz, "MaxMind DatabaseReader class should be available on the classpath");
    }
}
