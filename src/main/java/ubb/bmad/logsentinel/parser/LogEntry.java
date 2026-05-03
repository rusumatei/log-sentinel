package ubb.bmad.logsentinel.parser;

import java.time.LocalDateTime;

/**
 * Immutable representation of a log failure event.
 */
public record LogEntry(LocalDateTime timestamp, String ipAddress, String username) {
}
