package pl.ee.gameServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.repository.MatchRepository;

import java.util.List;


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

    public Match getMatch(Integer id) {
        return matchRepository.findById(id).get();
    }

    public void deleteMatch(Integer id) {
        matchRepository.deleteById(id);
    }

}
