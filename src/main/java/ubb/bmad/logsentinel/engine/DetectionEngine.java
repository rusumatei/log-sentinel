package ubb.bmad.logsentinel.engine;

import ubb.bmad.logsentinel.geo.GeoLookupService;
import ubb.bmad.logsentinel.parser.LogEntry;
import ubb.bmad.logsentinel.parser.LogParser;
import java.time.ZoneOffset;
import java.util.*;

public class DetectionEngine {
    private final int threshold;
    private final long windowMillis;
    private final IncidentRepository repository;
    private static final int MAX_TRACKED_IPS = 5000;
    
    // Use a size-limited map to prevent unbounded state growth
    private final Map<String, Deque<Long>> failureTracker = new LinkedHashMap<>(MAX_TRACKED_IPS, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Deque<Long>> eldest) {
            return size() > MAX_TRACKED_IPS;
        }
    };

    public DetectionEngine(int threshold, int windowMinutes, IncidentRepository repository) {
        this.threshold = threshold;
        this.windowMillis = (long) windowMinutes * 60 * 1000;
        this.repository = repository;
    }

    /**
     * Processes a log entry and checks if it triggers a brute-force incident.
     * 
     * @param entry The log entry to process.
     * @return An Optional containing the Incident if the threshold is met, otherwise empty.
     */
    public Optional<Incident> addFailure(LogEntry entry) {
        String ip = entry.ipAddress();
        long timestamp = entry.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli();

        Deque<Long> window = failureTracker.computeIfAbsent(ip, k -> new ArrayDeque<>());
        
        // Handle out-of-order logs to maintain window integrity
        if (!window.isEmpty() && timestamp < window.peekLast()) {
            // If log is older than the latest, we skip to preserve sliding window logic
            // In a production system, we might re-sort or use a PriorityQueue
            return Optional.empty();
        }

        window.addLast(timestamp);

        // Slide the window: remove timestamps older than (current - windowMillis)
        long windowStart = timestamp - windowMillis;
        while (!window.isEmpty() && window.peekFirst() < windowStart) {
            window.removeFirst();
        }

        if (window.size() >= threshold) {
            String location;
            GeoLookupService geoService = GeoLookupService.getInstance();
            if (geoService.isEnabled()) {
                location = LogParser.sanitize(geoService.lookup(ip));
            } else {
                location = "Geo-Intelligence Disabled";
            }

            Incident incident = Incident.builder()
                    .ipAddress(LogParser.sanitize(ip))
                    .hitCount(window.size())
                    .firstSeen(java.time.Instant.ofEpochMilli(window.peekFirst()).atOffset(ZoneOffset.UTC).toLocalDateTime())
                    .lastSeen(java.time.Instant.ofEpochMilli(window.peekLast()).atOffset(ZoneOffset.UTC).toLocalDateTime())
                    .location(location)
                    .build();

            if (repository != null) {
                repository.findByIpAddress(incident.getIpAddress()).ifPresent(existing -> 
                    incident.setId(existing.getId())
                );
                repository.save(incident);
            }

            return Optional.of(incident);
        }

        return Optional.empty();
    }
}
