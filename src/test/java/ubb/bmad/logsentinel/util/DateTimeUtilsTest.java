package ubb.bmad.logsentinel.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    void shouldParseValidSyslogTimestamp() {
        String timestamp = "Oct 24 08:00:00";
        LocalDateTime result = DateTimeUtils.parseSyslogTimestamp(timestamp);
        assertNotNull(result);
        assertEquals(10, result.getMonthValue());
        assertEquals(24, result.getDayOfMonth());
        assertEquals(8, result.getHour());
    }

    @Test
    void shouldHandleInvalidTimestamp() {
        assertThrows(Exception.class, () -> DateTimeUtils.parseSyslogTimestamp("Invalid"));
    }
}
