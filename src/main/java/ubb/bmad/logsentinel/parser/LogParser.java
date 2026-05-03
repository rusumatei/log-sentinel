package ubb.bmad.logsentinel.parser;

import ubb.bmad.logsentinel.util.DateTimeUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    // Optimized regex to prevent backtracking issues
    private static final Pattern SSHD_FAILED_PATTERN = Pattern.compile(
            "^(\\w{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+\\S+\\s+sshd\\[\\d+\\]:\\s+Failed password for\\s+(?:invalid user\\s+)?(\\S+)\\s+from\\s+(\\d{1,3}(?:\\.\\d{1,3}){3})\\s+port\\s+\\d+(?:\\s+ssh2)?$"
    );

    public Optional<LogEntry> parse(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = SSHD_FAILED_PATTERN.matcher(line);
        if (matcher.matches()) {
            try {
                String timestampStr = matcher.group(1);
                String username = matcher.group(2);
                String ipAddress = matcher.group(3);

                return Optional.of(new LogEntry(
                        DateTimeUtils.parseSyslogTimestamp(timestampStr),
                        ipAddress,
                        username
                ));
            } catch (Exception e) {
                // Log and skip malformed entries after sanitizing input to prevent injection
                String sanitizedLine = sanitize(line);
                System.err.println("Warning: Failed to parse line: " + sanitizedLine + " Error: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    /**
     * Strips control characters and full ANSI escape sequences to prevent logging injection.
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        
        // 1. Strip full ANSI escape sequences: ESC [ (parameters) (command)
        // Regex: \u001B \[ [0-9;]* [a-zA-Z]
        String stripped = input.replaceAll("\u001B\\[[0-9;]*[a-zA-Z]", "?");
        
        // 2. Strip remaining non-printable characters
        return stripped.replaceAll("[^\\p{Print}]", "?");
    }
}
