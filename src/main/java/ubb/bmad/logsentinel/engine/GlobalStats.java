package ubb.bmad.logsentinel.engine;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStats {
    @Id
    @Builder.Default
    private String id = "SINGLETON";

    private int totalLinesProcessed;
    private int uniqueIpsScanned;
}
