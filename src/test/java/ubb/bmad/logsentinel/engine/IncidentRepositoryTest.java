package ubb.bmad.logsentinel.engine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository repository;

    @Test
    void shouldSaveAndFindIncident() {
        Incident incident = Incident.builder()
                .ipAddress("192.168.1.1")
                .hitCount(10)
                .firstSeen(LocalDateTime.now().minusMinutes(5))
                .lastSeen(LocalDateTime.now())
                .location("Cluj-Napoca, RO")
                .build();

        Incident saved = repository.save(incident);
        assertThat(saved.getId()).isNotNull();

        Optional<Incident> found = repository.findByIpAddress("192.168.1.1");
        assertThat(found).isPresent();
        assertThat(found.get().getHitCount()).isEqualTo(10);
        assertThat(found.get().getLocation()).isEqualTo("Cluj-Napoca, RO");
    }

    @Test
    void shouldReturnEmptyWhenIpNotFound() {
        Optional<Incident> found = repository.findByIpAddress("10.0.0.1");
        assertThat(found).isEmpty();
    }
}
