package ubb.bmad.logsentinel.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class H2ConsoleProfileTest {

    @Value("${spring.h2.console.enabled}")
    private boolean h2ConsoleEnabled;

    @Test
    void shouldBeDisabledByDefault() {
        assertThat(h2ConsoleEnabled).isFalse();
    }

    @SpringBootTest(properties = "spring.profiles.active=dev")
    @ActiveProfiles("dev")
    static class DevProfileTest {
        @Value("${spring.h2.console.enabled}")
        private boolean h2ConsoleEnabled;

        @Test
        void shouldBeEnabledInDevProfile() {
            assertThat(h2ConsoleEnabled).isTrue();
        }
    }
}
