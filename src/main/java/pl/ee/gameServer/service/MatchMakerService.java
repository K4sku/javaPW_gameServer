package pl.ee.gameServer.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

    private final LinkedBlockingQueue<QueuedPlayer> waitingPlayers = new LinkedBlockingQueue<>(100);

    public boolean addPlayerToQueue(Player player, Match match)
    {
        QueuedPlayer queuedPlayer = new QueuedPlayer(player, match.getPlayerOneShips());
        return waitingPlayers.offer(queuedPlayer);
    }

    public void makeMatch(){
        QueuedPlayer[] queuedPlayers = new QueuedPlayer[]{null, null};

        while (waitingPlayers.size() >= 2) {
            while ( queuedPlayers[0] == null || queuedPlayers[0].equals(waitingPlayers.peek())) {
                queuedPlayers[0] = waitingPlayers.poll();
            }
            queuedPlayers[1] = waitingPlayers.poll();
            Match newMatch = new Match();
            matchRepository.save(newMatch);
            newMatch.addPlayer(queuedPlayers[0]);

            playerRepository.save(queuedPlayers[0].getPlayer());
            playerRepository.save(queuedPlayers[1].getPlayer());
        }
    }

    @AllArgsConstructor
    private class QueuedPlayer {
        @Getter @Setter private Player player;
        @Getter @Setter private char[][] playerShips = new char[10][10];
    }
}
