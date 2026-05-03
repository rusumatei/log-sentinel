package ubb.bmad.logsentinel.dashboard.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ubb.bmad.logsentinel.dashboard.AppState;
import ubb.bmad.logsentinel.engine.Incident;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AlertServiceTest {

    @Test
    void shouldSubscribeToAlerts() {
        AppState state = mock(AppState.class);
        AlertService service = new AlertService(state);
        SseEmitter emitter = service.subscribe();
        assertNotNull(emitter);
    }

    @Test
    void shouldBroadcastIncidentToEmitters() {
        AppState state = mock(AppState.class);
        AlertService service = new AlertService(state);
        service.subscribe();
        
        Incident incident = Incident.builder()
                .ipAddress("1.2.3.4")
                .hitCount(5)
                .firstSeen(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .location("Test")
                .build();
        assertDoesNotThrow(() -> service.broadcast(incident));
    }
}
