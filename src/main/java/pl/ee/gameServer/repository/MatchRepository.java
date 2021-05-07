package pl.ee.gameServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ee.gameServer.model.Match;

public interface MatchRepository extends JpaRepository<Match, Integer> {
}
