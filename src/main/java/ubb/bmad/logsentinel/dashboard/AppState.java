package ubb.bmad.logsentinel.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ubb.bmad.logsentinel.dashboard.service.AlertService;
import ubb.bmad.logsentinel.engine.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;

/**
 * Spring-managed container for application state.
 * Integrates with JPA repositories to ensure persistence across restarts.
 */
@Component
public class AppState {
    private static AppState instance; // Legacy support for non-Spring components

    private AlertService alertService;
    private final IncidentRepository incidentRepository;
    private final GlobalStatsRepository globalStatsRepository;
    private final SeenIpRepository seenIpRepository;

    private final AtomicInteger totalLinesProcessed = new AtomicInteger(0);
    private final Map<String, Incident> flaggedIncidents = new ConcurrentHashMap<>();
    private long lastSavedLines = 0;

    @Autowired
    public AppState(IncidentRepository incidentRepository, 
                    GlobalStatsRepository globalStatsRepository,
                    SeenIpRepository seenIpRepository) {
        this.incidentRepository = incidentRepository;
        this.globalStatsRepository = globalStatsRepository;
        this.seenIpRepository = seenIpRepository;
        instance = this;
    }

    public static AppState getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() {
        // Hydrate state from database
        incidentRepository.findAll().forEach(incident -> 
            flaggedIncidents.put(incident.getIpAddress(), incident)
        );

        globalStatsRepository.findById("SINGLETON").ifPresent(stats -> {
            totalLinesProcessed.set(stats.getTotalLinesProcessed());
            lastSavedLines = stats.getTotalLinesProcessed();
        });
    }

    public void incrementProcessedLines() {
        int current = totalLinesProcessed.incrementAndGet();
        // Periodically sync to DB to balance performance and persistence
        if (current - lastSavedLines >= 100) {
            saveGlobalStats();
        }
    }

    public void trackIp(String ip) {
        if (!seenIpRepository.existsById(ip)) {
            seenIpRepository.save(new SeenIp(ip));
        }
    }

    public void flagIncident(String ip, Incident incident) {
        flaggedIncidents.put(ip, incident);
        if (alertService != null) {
            alertService.broadcast(incident);
        }
    }

    public void saveGlobalStats() {
        globalStatsRepository.save(GlobalStats.builder()
                .id("SINGLETON")
                .totalLinesProcessed(totalLinesProcessed.get())
                .uniqueIpsScanned((int) seenIpRepository.count())
                .build());
        lastSavedLines = totalLinesProcessed.get();
    }

    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    public int getTotalLinesProcessed() {
        return totalLinesProcessed.get();
    }

    public int getUniqueIpsScanned() {
        return (int) seenIpRepository.count();
    }

    public int getThreatCount() {
        return flaggedIncidents.size();
    }

    public Map<String, Incident> getFlaggedIncidents() {
        return flaggedIncidents;
    }
}
