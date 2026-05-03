package ubb.bmad.logsentinel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import ubb.bmad.logsentinel.config.ConfigLoader;
import ubb.bmad.logsentinel.dashboard.AppState;
import ubb.bmad.logsentinel.engine.DetectionEngine;
import ubb.bmad.logsentinel.engine.Incident;
import ubb.bmad.logsentinel.engine.LogTailer;
import ubb.bmad.logsentinel.geo.GeoLookupService;
import ubb.bmad.logsentinel.parser.LogEntry;
import ubb.bmad.logsentinel.parser.LogParser;
import ubb.bmad.logsentinel.report.SecurityReporter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Optional;

@SpringBootApplication
@EnableScheduling
public class Main implements CommandLineRunner {

    @Autowired
    private ubb.bmad.logsentinel.engine.IncidentRepository incidentRepository;

    @Autowired
    private AppState state;

    public static void main(String[] args) {
        ConfigLoader config = new ConfigLoader();
        config.load(args);

        SpringApplication app = new SpringApplication(Main.class);
        
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        if (config.isLiveMode()) {
            app.setWebApplicationType(WebApplicationType.SERVLET);
            props.put("server.port", "8080");
        } else {
            app.setWebApplicationType(WebApplicationType.NONE);
        }

        // Inject sensitive credentials if provided via ConfigLoader (CLI/Env/Properties)
        if (config.getDbUsername() != null) props.put("spring.datasource.username", config.getDbUsername());
        if (config.getDbPassword() != null) props.put("spring.datasource.password", config.getDbPassword());
        if (config.getDashboardUser() != null) props.put("dashboard.admin.user", config.getDashboardUser());
        if (config.getDashboardPassword() != null) props.put("dashboard.admin.password", config.getDashboardPassword());

        if (!props.isEmpty()) {
            app.setDefaultProperties(props);
        }
        
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        ConfigLoader config = new ConfigLoader();
        config.load(args);

        String logPath = config.getLogFile();
        File logFile = new File(logPath);

        // Fallback for local dev if log is in parent dir
        if (!logFile.exists() && new File("../" + logPath).exists()) {
            logPath = "../" + logPath;
            logFile = new File(logPath);
        }

        if (!logFile.exists() && !config.isLiveMode()) {
            System.err.println("Error: Log file not found at " + logPath);
            System.out.println("Usage: java -jar LogSentinel.jar [-f <file>] [-t <threshold>] [-w <window_minutes>] [-live]");
            return;
        }

        System.out.println("Starting LogSentinel Analysis...");
        System.out.println("Mode:      " + (config.isLiveMode() ? "LIVE MONITORING" : "BATCH PROCESSING"));
        System.out.println("Log File:  " + logPath);
        System.out.println("Threshold: " + config.getThreshold() + " failures");
        System.out.println("Window:    " + config.getWindowMinutes() + " minutes");
        System.out.println("Geo DB:    " + config.getGeoIpDatabasePath());
        System.out.println("----------------------------------------");

        String geoDbPath = config.getGeoIpDatabasePath();
        File geoDbFile = new File(geoDbPath);
        if (!geoDbFile.exists() && new File("../" + geoDbPath).exists()) {
            geoDbPath = "../" + geoDbPath;
        }

        // Initialize Geo-Spatial Service
        try {
            GeoLookupService.init(geoDbPath);
            if (GeoLookupService.getInstance().isEnabled()) {
                System.out.println("Geo-Spatial Threat Intelligence Enabled.");
            } else {
                System.err.println("CRITICAL WARNING: Geo-Spatial Intelligence is DISABLED (Service uninitialized).");
            }
        } catch (IOException e) {
            System.err.println("CRITICAL WARNING: Geo-Spatial Intelligence is DISABLED. Database error: " + e.getMessage());
        }

        LogParser parser = new LogParser();
        DetectionEngine engine = new DetectionEngine(config.getThreshold(), config.getWindowMinutes(), incidentRepository);
        SecurityReporter reporter = new SecurityReporter();

        if (config.isLiveMode()) {
            LogTailer tailer = new LogTailer(true); // Jump to end for live mode

            // Register Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n----------------------------------------");
                System.out.println("Shutdown signal received. Finalizing analysis...");
                tailer.stop();
                
                // Ensure final stats are persisted
                state.saveGlobalStats();

                try {
                    reporter.generateReport(
                            config.getReportOutput(),
                            state.getFlaggedIncidents().values(),
                            state.getUniqueIpsScanned(),
                            state.getTotalLinesProcessed()
                    );
                } catch (Exception e) {
                    System.err.println("Warning: Failed to generate final shutdown report: " + e.getMessage());
                }
                System.out.println("Session Summary: Processed " + state.getTotalLinesProcessed() + " lines, found " + state.getThreatCount() + " threats.");
                
                // Close Geo-Spatial Service
                try {
                    GeoLookupService.getInstance().close();
                } catch (IOException e) {
                    System.err.println("Error closing Geo-Spatial database: " + e.getMessage());
                }

                System.out.println("LogSentinel stopped.");
            }));

            tailer.startTailing(logPath, line -> {
                state.incrementProcessedLines();
                Optional<LogEntry> entry = parser.parse(line);
                if (entry.isPresent()) {
                    String ip = entry.get().ipAddress();
                    state.trackIp(ip);

                    Optional<Incident> incident = engine.addFailure(entry.get());
                    if (incident.isPresent()) {
                        System.out.println("[ALERT] Brute-force detected from IP: " + ip);
                        state.flagIncident(ip, incident.get());
                        // Event-driven reporting: update report on every hit
                        reporter.generateReport(
                                config.getReportOutput(),
                                state.getFlaggedIncidents().values(),
                                state.getUniqueIpsScanned(),
                                state.getTotalLinesProcessed()
                        );
                    }
                }
            });
        } else {
            // V1 Batch Processing Mode (Hardened with Bounded Ingestion)
            try {
                try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
                    String line;
                    while ((line = LogTailer.readBoundedLine(raf)) != null) {
                        state.incrementProcessedLines();
                        Optional<LogEntry> entry = parser.parse(line);
                        if (entry.isPresent()) {
                            String ip = entry.get().ipAddress();
                            state.trackIp(ip);

                            Optional<Incident> incident = engine.addFailure(entry.get());
                            if (incident.isPresent()) {
                                state.flagIncident(ip, incident.get());
                            }
                        }
                    }

                    // Save stats after batch processing
                    state.saveGlobalStats();

                    reporter.generateReport(
                            config.getReportOutput(),
                            state.getFlaggedIncidents().values(),
                            state.getUniqueIpsScanned(),
                            state.getTotalLinesProcessed()
                    );

                    System.out.println("Analysis Complete. Found " + state.getThreatCount() + " threats.");
                }
            } catch (IOException e) {
                System.err.println("Fatal Error: Could not read log file. " + e.getMessage());
            } finally {
                // Guaranteed cleanup of Geo-Spatial database handle
                try {
                    GeoLookupService.getInstance().close();
                } catch (IOException e) {
                    System.err.println("Error closing Geo-Spatial database: " + e.getMessage());
                }
            }
        }
    }
}
