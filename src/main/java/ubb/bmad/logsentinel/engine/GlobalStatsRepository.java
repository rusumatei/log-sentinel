package ubb.bmad.logsentinel.engine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalStatsRepository extends JpaRepository<GlobalStats, String> {
}
