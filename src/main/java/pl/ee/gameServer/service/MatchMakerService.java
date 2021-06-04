package pl.ee.gameServer.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.MatchRepository;
import pl.ee.gameServer.repository.PlayerRepository;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Transactional
public class MatchMakerService {
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    private final LinkedBlockingQueue<QueuedPlayer> waitingPlayers = new LinkedBlockingQueue<>(100);

    public MatchMakerService(MatchRepository matchRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    public void addPlayerToQueue(Player player, Match match)
    {
        QueuedPlayer queuedPlayer = new QueuedPlayer(player, match.getPlayerOneShips());
        waitingPlayers.offer(queuedPlayer);
        matchPlayers();
    }

    public void matchPlayers(){
        QueuedPlayer[] queuedPlayers = new QueuedPlayer[]{null, null};

        while (waitingPlayers.size() >= 2) {
            while ( queuedPlayers[0] == null || queuedPlayers[0].equals(waitingPlayers.peek())) {
                queuedPlayers[0] = waitingPlayers.poll();
            }
            queuedPlayers[1] = waitingPlayers.poll();
            initGame(queuedPlayers[0].getPlayer(), queuedPlayers[0].getPlayerShips(),
                    queuedPlayers[1].getPlayer(), queuedPlayers[1].getPlayerShips());
        }
    }

    public void initGame(Player playerOne, char[][] playerOneShips, Player playerTwo, char[][] playerTwoShips) {
        Match match = new Match();
        match.setPlayerOneShips(playerOneShips);
        match.setPlayerTwoShips(playerTwoShips);
        Random rd = new Random();
        if (rd.nextBoolean()) {
            match.setShootingPlayer(playerOne);
        } else {
            match.setShootingPlayer(playerTwo);
        }

        matchRepository.save(match);
        playerOne.addPlayerOneGame(match);
        playerRepository.save(playerOne);
        playerTwo.addPlayerTwoGame(match);
        playerRepository.save(playerTwo);
    }

    @AllArgsConstructor
    private static class QueuedPlayer {
        @Getter @Setter private Player player;
        @Getter @Setter private char[][] playerShips;

        @Override
        public boolean equals(Object o){
            if (this == o) return true;
            if (!(o instanceof QueuedPlayer)) return false;
            return player != null && player.equals(((QueuedPlayer) o).getPlayer()) && playerShips != null && Arrays.deepEquals(playerShips, ((QueuedPlayer) o).getPlayerShips());
        }
    }
}
