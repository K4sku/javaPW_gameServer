package pl.ee.gameServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.MatchRepository;
import pl.ee.gameServer.repository.PlayerRepository;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        Player[] players = new Player[]{null, null};

        while (watingPlayers.size() >= 2) {
            while ( players[0] == null || players[0].equals(watingPlayers.peek())) {
                players[0] = watingPlayers.poll();
            }
            players[1] = watingPlayers.poll();
            Match newMatch = new Match();
            newMatch.addPlayers(players);
//            matchRepository.save(newMatch);

            playerRepository.saveAll(Arrays.asList(players));
        }
    }
}
