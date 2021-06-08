package pl.ee.gameServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.repository.MatchRepository;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;

    public List<Match> listAllMatch() {
        return matchRepository.findAll();
    }

    public void saveMatch(Match match) {
        matchRepository.save(match);
    }

    public Match getMatch(UUID uuid) {
        return matchRepository.findById(uuid).get();
    }

    public void deleteMatch(UUID uuid) {
        matchRepository.deleteById(uuid);
    }

    public List<Match> listNotStartedGames() {return matchRepository.findNotStartedGames(); }
}
