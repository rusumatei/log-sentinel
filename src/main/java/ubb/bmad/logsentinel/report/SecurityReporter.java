package ubb.bmad.logsentinel.report;

import ubb.bmad.logsentinel.engine.Incident;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Collection;

public class SecurityReporter {

    public synchronized void generateReport(String outputPath, Collection<Incident> incidents, int totalIpsScanned, int totalLinesProcessed) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("======================================================");
            writer.println("            LOGSENTINEL SECURITY REPORT");
            writer.println("======================================================");
            writer.println("Report Generated: " + LocalDateTime.now());
            writer.println();
            
            writer.println("--- EXECUTIVE SUMMARY ---");
            writer.println("Lines Processed:    " + totalLinesProcessed);
            writer.println("Total unique IPs:   " + totalIpsScanned);
            writer.println("Flagged Threats:    " + incidents.size());
            writer.println("Status:             " + (incidents.isEmpty() ? "CLEAR" : "ATTENTION REQUIRED"));
            writer.println();

            if (!incidents.isEmpty()) {
                writer.println("--- THREAT DETAILS ---");
                for (Incident incident : incidents) {
                    writer.println("Suspect IP:     " + incident.getIpAddress());
                    writer.println("Geo-Location:   " + incident.getLocation());
                    writer.println("Failure Count:  " + incident.getHitCount());
                    writer.println("Window Start:   " + incident.getFirstSeen());
                    writer.println("Window End:     " + incident.getLastSeen());
                    writer.println("------------------------------------------------------");
                }
            } else {
                writer.println("No brute-force patterns detected in the provided log.");
            }
            
            writer.println();
            writer.println("End of Report");
            System.out.println("Report successfully written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error writing security report: " + e.getMessage());
        }
    }
}
