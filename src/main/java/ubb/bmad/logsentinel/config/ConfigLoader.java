package ubb.bmad.logsentinel.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private String logFile = "sample_server.log";
    private int threshold = 5;
    private int windowMinutes = 2;
    private String reportOutput = "threat_report.txt";
    private String geoIpDatabasePath = "GeoLite2-City.mmdb";
    private boolean liveMode = false;
    private String dbUsername;
    private String dbPassword;
    private String dashboardUser;
    private String dashboardPassword;

    public void load(String[] args) {
        // 1. Load from environment variables
        dbUsername = System.getenv("DB_USERNAME");
        dbPassword = System.getenv("DB_PASSWORD");
        dashboardUser = System.getenv("DASHBOARD_ADMIN_USER");
        dashboardPassword = System.getenv("DASHBOARD_ADMIN_PASSWORD");

        // 2. Load from properties file if it exists
        loadFromProperties("config.properties");

        // 3. Override with CLI arguments
        parseArgs(args);
    }

    private void loadFromProperties(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path);
             java.io.BufferedInputStream bis = new java.io.BufferedInputStream(fis)) {
            props.load(bis);
            logFile = props.getProperty("log.file", logFile);
            threshold = Integer.parseInt(props.getProperty("detection.threshold", String.valueOf(threshold)));
            windowMinutes = Integer.parseInt(props.getProperty("detection.window_minutes", String.valueOf(windowMinutes)));
            reportOutput = props.getProperty("report.output", reportOutput);
            dbUsername = props.getProperty("db.username", dbUsername);
            dbPassword = props.getProperty("db.password", dbPassword);
            dashboardUser = props.getProperty("dashboard.user", dashboardUser);
            dashboardPassword = props.getProperty("dashboard.password", dashboardPassword);
        } catch (IOException | NumberFormatException e) {
            // Optional: Log that we are using defaults or CLI
        }
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            try {
                switch (args[i]) {
                    case "-f" -> logFile = args[++i];
                    case "-t" -> threshold = Integer.parseInt(args[++i]);
                    case "-w" -> windowMinutes = Integer.parseInt(args[++i]);
                    case "-o" -> reportOutput = args[++i];
                    case "-geo" -> geoIpDatabasePath = args[++i];
                    case "-dbuser" -> dbUsername = args[++i];
                    case "-dbpass" -> dbPassword = args[++i];
                    case "-dashuser" -> dashboardUser = args[++i];
                    case "-dashpass" -> dashboardPassword = args[++i];
                    case "-live" -> liveMode = true;
                }
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                System.err.println("Warning: Invalid argument format for " + args[i - 1] + ". Using previous value.");
            }
        }
    }

    public String getLogFile() { return logFile; }
    public int getThreshold() { return threshold; }
    public int getWindowMinutes() { return windowMinutes; }
    public String getReportOutput() { return reportOutput; }
    public String getGeoIpDatabasePath() { return geoIpDatabasePath; }
    public boolean isLiveMode() { return liveMode; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }
    public String getDashboardUser() { return dashboardUser; }
    public String getDashboardPassword() { return dashboardPassword; }
}
