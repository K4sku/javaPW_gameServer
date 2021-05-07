package pl.ee.gameServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.MatchRepository;
import pl.ee.gameServer.repository.PlayerRepository;

import java.util.concurrent.LinkedBlockingQueue;

@Service
@Transactional
public class MatchMakerService {
    @Autowired
    private MatchRepository matchRepository;
    private PlayerRepository playerRepository;

    private final LinkedBlockingQueue<Player> watingPlayers = new LinkedBlockingQueue<>(100);

    public boolean addPlayerToQueue(Player player) {
        return watingPlayers.offer(player);
    }

    public void makeMatch(){
        Player player1 = null;
        Player player2;
        while (watingPlayers.size() >= 2) {
            while ( player1 == null || player1.equals(watingPlayers.peek())) {
                player1 = watingPlayers.poll();
            }
            player2 = watingPlayers.poll();
            Match newMatch = new Match();
            player1.addMatch(newMatch);
            assert player2 != null;
            player2.addMatch(newMatch);
            playerRepository.save(player1);
            playerRepository.save(player2);
        }
    }
}
