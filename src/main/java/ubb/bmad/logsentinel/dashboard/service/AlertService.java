package ubb.bmad.logsentinel.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ubb.bmad.logsentinel.dashboard.AppState;
import ubb.bmad.logsentinel.engine.Incident;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for pushing real-time alerts to connected web clients via SSE.
 */
@Service
public class AlertService {
    final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AppState state;

    @Autowired
    public AlertService(AppState state) {
        this.state = state;
        state.setAlertService(this);
    }

    public SseEmitter subscribe() {
        // Enforce a strict 30-second timeout to prevent SSE exhaustion attacks
        SseEmitter emitter = new SseEmitter(30_000L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    /**
     * Heartbeat to keep connections alive and prune dead emitters proactively.
     */
    @Scheduled(fixedRate = 20_000)
    public void sendHeartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("keep-alive"));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void broadcast(Incident incident) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(incident);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
