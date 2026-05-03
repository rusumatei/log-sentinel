package ubb.bmad.logsentinel.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ubb.bmad.logsentinel.dashboard.AppState;
import ubb.bmad.logsentinel.engine.Incident;
import ubb.bmad.logsentinel.engine.IncidentRepository;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for retrieving live session statistics and historical incidents.
 */
@RestController
@RequestMapping("/api")
public class StatsController {

    private final IncidentRepository incidentRepository;
    private final AppState state;

    @Autowired
    public StatsController(IncidentRepository incidentRepository, AppState state) {
        this.incidentRepository = incidentRepository;
        this.state = state;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
            "totalLinesProcessed", state.getTotalLinesProcessed(),
            "uniqueIpsScanned", state.getUniqueIpsScanned(),
            "threatCount", state.getThreatCount()
        );
    }

    @GetMapping("/incidents/history")
    public List<Incident> getHistory() {
        return incidentRepository.findAll();
    }
}
