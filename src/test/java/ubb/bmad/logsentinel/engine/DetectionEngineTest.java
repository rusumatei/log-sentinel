package ubb.bmad.logsentinel.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ubb.bmad.logsentinel.geo.GeoLookupService;
import ubb.bmad.logsentinel.parser.LogEntry;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DetectionEngineTest {

    private GeoLookupService mockGeoService;

    @BeforeEach
    void setUp() throws Exception {
        mockGeoService = mock(GeoLookupService.class);
        
        // Inject the mock into the singleton instance field of GeoLookupService
        Field instanceField = GeoLookupService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, mockGeoService);
    }

    @Test
    void shouldDetectIncidentWithGeoEnrichment() {
        DetectionEngine engine = new DetectionEngine(3, 1, null);
        String ip = "1.1.1.1";
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 8, 0, 0);
        
        when(mockGeoService.isEnabled()).thenReturn(true);
        when(mockGeoService.lookup(ip)).thenReturn("Romania, Cluj");

        engine.addFailure(new LogEntry(now, ip, "user"));
        engine.addFailure(new LogEntry(now.plusSeconds(30), ip, "user"));
        Optional<Incident> incident = engine.addFailure(new LogEntry(now.plusSeconds(45), ip, "user"));

        assertTrue(incident.isPresent());
        assertEquals(3, incident.get().getHitCount());
        assertEquals(ip, incident.get().getIpAddress());
        assertEquals("Romania, Cluj", incident.get().getLocation());
        verify(mockGeoService, atLeastOnce()).lookup(ip);
    }

    @Test
    void shouldFallbackToInternalUnknownIfGeoServiceFails() {
        DetectionEngine engine = new DetectionEngine(2, 1, null);
        String ip = "127.0.0.1";
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 8, 0, 0);
        
        when(mockGeoService.isEnabled()).thenReturn(true);
        when(mockGeoService.lookup(ip)).thenReturn("Internal/Unknown");

        engine.addFailure(new LogEntry(now, ip, "user"));
        Optional<Incident> incident = engine.addFailure(new LogEntry(now.plusSeconds(10), ip, "user"));

        assertTrue(incident.isPresent());
        assertEquals("Internal/Unknown", incident.get().getLocation());
    }

    @Test
    void shouldFallbackIfGeoServiceIsNull() throws Exception {
        // Force instance to null
        Field instanceField = GeoLookupService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        DetectionEngine engine = new DetectionEngine(1, 1, null);
        String ip = "1.1.1.1";
        LocalDateTime now = LocalDateTime.now();

        Optional<Incident> incident = engine.addFailure(new LogEntry(now, ip, "user"));

        assertTrue(incident.isPresent());
        assertEquals("Geo-Intelligence Disabled", incident.get().getLocation());
    }

    @Test
    void shouldNotDetectIncidentWhenBelowThreshold() {
        DetectionEngine engine = new DetectionEngine(3, 1, null);
        String ip = "1.1.1.1";
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 8, 0, 0);

        engine.addFailure(new LogEntry(now, ip, "user"));
        Optional<Incident> incident = engine.addFailure(new LogEntry(now.plusSeconds(30), ip, "user"));

        assertFalse(incident.isPresent());
    }

    @Test
    void shouldPurgeOldTimestampsAndNotDetect() {
        DetectionEngine engine = new DetectionEngine(2, 1, null);
        String ip = "1.1.1.1";
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 8, 0, 0);

        engine.addFailure(new LogEntry(now, ip, "user"));
        Optional<Incident> incident = engine.addFailure(new LogEntry(now.plusSeconds(70), ip, "user"));

        assertFalse(incident.isPresent());
    }
}
