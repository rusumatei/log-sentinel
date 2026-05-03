package ubb.bmad.logsentinel.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ubb.bmad.logsentinel.engine.Incident;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityReporterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldIncludeGeoLocationInReport() throws IOException {
        SecurityReporter reporter = new SecurityReporter();
        Path reportPath = tempDir.resolve("test_report.txt");
        
        Incident incident = Incident.builder()
                .ipAddress("1.2.3.4")
                .hitCount(5)
                .firstSeen(LocalDateTime.now().minusMinutes(10))
                .lastSeen(LocalDateTime.now())
                .location("test-country, test-city")
                .build();

        reporter.generateReport(
                reportPath.toString(),
                List.of(incident),
                1,
                100
        );

        assertTrue(Files.exists(reportPath));
        String content = Files.readString(reportPath);
        
        assertTrue(content.contains("Suspect IP:     1.2.3.4"));
        assertTrue(content.contains("Geo-Location:   test-country, test-city"));
        assertTrue(content.contains("Failure Count:  5"));
    }

    @Test
    void shouldHandleEmptyIncidents() throws IOException {
        SecurityReporter reporter = new SecurityReporter();
        Path reportPath = tempDir.resolve("empty_report.txt");

        reporter.generateReport(
                reportPath.toString(),
                Collections.emptyList(),
                0,
                0
        );

        assertTrue(Files.exists(reportPath));
        String content = Files.readString(reportPath);
        assertTrue(content.contains("No brute-force patterns detected"));
    }
}
