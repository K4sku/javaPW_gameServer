package pl.ee.gameServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.MatchRepository;
import pl.ee.gameServer.repository.PlayerRepository;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Transactional
public class MatchMakerService {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private PlayerRepository playerRepository;

    private final LinkedBlockingQueue<Player> waitingPlayers = new LinkedBlockingQueue<>(100);

    public boolean addPlayerToQueue(Player player) {
        return waitingPlayers.offer(player);
    }

    public void makeMatch(){
        Player[] players = new Player[]{null, null};

        while (waitingPlayers.size() >= 2) {
            while ( players[0] == null || players[0].equals(waitingPlayers.peek())) {
                players[0] = waitingPlayers.poll();
            }
            players[1] = waitingPlayers.poll();
            Match newMatch = new Match();
            matchRepository.save(newMatch);
            newMatch.addPlayers(players);
            playerRepository.saveAll(Arrays.asList(players));
        }
    }
}
