package ubb.bmad.logsentinel.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ubb.bmad.logsentinel.dashboard.AppState;
import ubb.bmad.logsentinel.parser.LogEntry;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class DetectionEnginePersistenceTest {

    @Autowired
    private IncidentRepository repository;

    @Autowired
    private AppState state;

    private DetectionEngine engine;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        engine = new DetectionEngine(2, 5, repository);
    }

    @Test
    void shouldPersistIncidentToDatabase() {
        String ip = "1.2.3.4";
        LocalDateTime now = LocalDateTime.now();

        engine.addFailure(new LogEntry(now, ip, "user"));
        Optional<Incident> incident = engine.addFailure(new LogEntry(now.plusSeconds(1), ip, "user"));

        assertThat(incident).isPresent();
        
        // Verify in DB
        Optional<Incident> stored = repository.findByIpAddress(ip);
        assertThat(stored).isPresent();
        assertThat(stored.get().getHitCount()).isEqualTo(2);
        assertThat(stored.get().getIpAddress()).isEqualTo(ip);
    }

    @Test
    void shouldUpdateExistingIncidentInDatabase() {
        String ip = "5.6.7.8";
        LocalDateTime now = LocalDateTime.now();

        // Trigger first incident
        engine.addFailure(new LogEntry(now, ip, "user"));
        engine.addFailure(new LogEntry(now.plusSeconds(1), ip, "user"));
        
        // Trigger second hit for same incident
        Optional<Incident> updated = engine.addFailure(new LogEntry(now.plusSeconds(2), ip, "user"));

        assertThat(updated).isPresent();
        assertThat(updated.get().getHitCount()).isEqualTo(3);

        // Verify only ONE record in DB with updated hit count
        assertThat(repository.count()).isEqualTo(1);
        Optional<Incident> stored = repository.findByIpAddress(ip);
        assertThat(stored).isPresent();
        assertThat(stored.get().getHitCount()).isEqualTo(3);
    }

    @Test
    void shouldLoadHistoricalDataIntoAppState() {
        // Pre-fill DB
        repository.save(Incident.builder()
                .ipAddress("10.0.0.1")
                .hitCount(5)
                .firstSeen(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .location("Test")
                .build());

        state.init();

        assertThat(state.getFlaggedIncidents()).containsKey("10.0.0.1");
        assertThat(state.getThreatCount()).isGreaterThanOrEqualTo(1);
    }
}
