package ubb.bmad.logsentinel.engine;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Captures the details of a detected brute-force incident.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private int hitCount;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private String location;
}
