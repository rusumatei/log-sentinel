package ubb.bmad.logsentinel.parser;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class LogParserTest {
    private final LogParser parser = new LogParser();

    @Test
    void shouldParseValidSshdFailure() {
        String line = "Oct 24 08:12:30 server-prod sshd[1423]: Failed password for root from 192.168.1.45 port 55432 ssh2";
        Optional<LogEntry> entry = parser.parse(line);

        assertTrue(entry.isPresent());
        assertEquals("192.168.1.45", entry.get().ipAddress());
        assertEquals("root", entry.get().username());
        assertEquals(10, entry.get().timestamp().getMonthValue());
        assertEquals(24, entry.get().timestamp().getDayOfMonth());
    }

    @Test
    void shouldParseInvalidUserFailure() {
        String line = "Oct 24 08:12:33 server-prod sshd[1425]: Failed password for invalid user admin from 10.0.0.5 port 44321 ssh2";
        Optional<LogEntry> entry = parser.parse(line);

        assertTrue(entry.isPresent());
        assertEquals("10.0.0.5", entry.get().ipAddress());
        assertEquals("admin", entry.get().username());
    }

    @Test
    void shouldIgnoreUnrelatedLines() {
        String line = "Oct 24 08:12:35 server-prod sshd[1426]: Accepted password for root from 192.168.1.45 port 55433 ssh2";
        Optional<LogEntry> entry = parser.parse(line);

        assertFalse(entry.isPresent());
    }

    @Test
    void shouldHandleMalformedLines() {
        String line = "This is not a log entry";
        Optional<LogEntry> entry = parser.parse(line);

        assertFalse(entry.isPresent());
    }

    @Test
    void shouldSanitizeControlCharacters() throws Exception {
        // Use reflection to test private sanitize method
        java.lang.reflect.Method sanitizeMethod = LogParser.class.getDeclaredMethod("sanitize", String.class);
        sanitizeMethod.setAccessible(true);
        
        // 1. Check ANSI Clear Screen (\u001B[2J)
        String input1 = "malicious\u001B[2Jusername"; 
        String result1 = (String) sanitizeMethod.invoke(parser, input1);
        assertFalse(result1.contains("\u001B"));
        assertFalse(result1.contains("[2J")); // Should be fully stripped
        assertTrue(result1.contains("malicious?username"));

        // 2. Check ANSI Color Code (\u001B[31m)
        String input2 = "\u001B[31mRED_ALERT";
        String result2 = (String) sanitizeMethod.invoke(parser, input2);
        assertFalse(result2.contains("\u001B"));
        assertFalse(result2.contains("[31m"));
        assertEquals("?RED_ALERT", result2);
    }
}
