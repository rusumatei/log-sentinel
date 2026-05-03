package ubb.bmad.logsentinel.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class DateTimeUtils {
    // Syslog format: Oct 24 08:12:30
    private static final DateTimeFormatter SYSLOG_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("MMM[ ]d HH:mm:ss")
            .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().getYear())
            .toFormatter(Locale.ENGLISH);

    public static LocalDateTime parseSyslogTimestamp(String timestampStr) {
        // Handle single-digit days which might have an extra space: "Oct  4" vs "Oct 24"
        String normalized = timestampStr.trim().replaceAll(" +", " ");
        return LocalDateTime.parse(normalized, SYSLOG_FORMATTER);
    }
}
