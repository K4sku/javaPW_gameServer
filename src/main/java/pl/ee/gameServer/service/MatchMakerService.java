package pl.ee.gameServer.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Transactional
public class MatchMakerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchMakerService.class);
    private final MatchService matchService;
    private final PlayerService playerService;
    private final LinkedBlockingQueue<QueuedPlayer> waitingPlayers = new LinkedBlockingQueue<>(100);
    private final LinkedBlockingQueue<Match> waitingMatches = new LinkedBlockingQueue<>(100);

    public MatchMakerService(MatchService matchService, PlayerService playerService) {
        this.matchService = matchService;
        this.playerService = playerService;
        loadWaitingGames();
    }

    public void addPlayerToQueue(Player player, char[][] board) {
        LOGGER.info("Adding player {} : {} to matchmaking queue. Players waiting in queue: {}", player.getName(), player.getUuid(), waitingPlayers.size() + 1);
        QueuedPlayer queuedPlayer = new QueuedPlayer(player, board);
        waitingPlayers.offer(queuedPlayer);
    }

    public void addPlayerToQueue(UUID playerUuid, char[][] board) {
        Player player = playerService.getPlayer(playerUuid);
        Hibernate.initialize(player.getPlayerOneGames());
        LOGGER.info("Adding player {} : {} to matchmaking queue. Players waiting in queue: {}", player.getName(), player.getUuid(), waitingPlayers.size() + 1);
        QueuedPlayer queuedPlayer = new QueuedPlayer(player, board);
        waitingPlayers.offer(queuedPlayer);
    }

    public Match matchPlayers() {
        QueuedPlayer playerOne = waitingPlayers.poll(); //poll p1 from queue head
        if (playerOne != null) {
            LOGGER.trace("Pool playerOne from queue {}", playerOne.getPlayer().getUuid());
            QueuedPlayer playerTwo = waitingPlayers.poll(); //poll p2 from queue head
            if (playerTwo != null) {
                LOGGER.trace("Pool playerTwo from queue {}", playerTwo.getPlayer().getUuid());
                if (playerOne.getPlayer().equals(playerTwo.getPlayer())) {
                    LOGGER.trace("PlayerOne and PlayerTwo are the same, putting playerOne at the end of queue");
                    waitingPlayers.offer(playerOne); // put p1 on back of queue
                    return matchPlayers();
                } else {
                    //take p2 from queue and make game with p1 and p2
                    LOGGER.trace("Two different players. Pooling playerTwo Starting new match.");
                    return initGame(playerOne.getPlayer(), playerOne.getPlayerShips(), playerTwo.getPlayer(), playerTwo.getPlayerShips());
                }

            } else if (waitingMatches.size() > 0) { //if there are matches waiting for 2nd player
                LOGGER.trace("No playerTwo to match, check waiting games.");
                int matchQueueSize = waitingMatches.size();
                Match waitingMatch;
                for (int i = 0; i < matchQueueSize; i++) { //iterate over queue
                    LOGGER.debug("waitingMatches queue size: {}, first match {}", waitingMatches.size(), waitingMatches.peek());
                    waitingMatch = waitingMatches.poll();
                    if (waitingMatch != null) { //avoid NullPointerException
                        LOGGER.trace("Pooling matches queue. There is game waiting matchUUID: {} | playerOne uuid: {}", waitingMatch.getUuid(), waitingMatch.getPlayerOne().getUuid());
                        if (waitingMatch.getPlayerOne().equals(playerOne.getPlayer())) { //avoid matching player with himself
                            LOGGER.trace("Player already in game, putting match back to queue");
                            waitingMatches.offer(waitingMatch); //put it back at the end of queue
                        } else {
                            LOGGER.trace("Adding player to the game as playerTwo");
                            fillPlayerTwo(waitingMatch, playerOne.getPlayer(), playerOne.getPlayerShips()); //add player to the game as P2
                        }
                        return waitingMatch;
                    }
                }
            } else {
                //make game with p1, place game to waitingMatches and return game to response
                LOGGER.trace("waitingMatches queue is empty. Start game with one player and add it to the queue.");
                Match match = initOnePlayerGame(playerOne.getPlayer(), playerOne.getPlayerShips());
                waitingMatches.offer(match);
                LOGGER.debug("waitingMatches queue size: {}, first match {}", waitingMatches.size(), waitingMatches.peek());
                return match;
            }
        }
        return null;
    }

    private Match initGame(Player playerOne, char[][] playerOneShips, Player playerTwo, char[][] playerTwoShips) {
        Match match = new Match();
        match.wipeShootsBoard();
        match.setUuid(UUID.randomUUID());
        LOGGER.trace("New game UUID: {}", match.getUuid());
        match.setPlayerOneShips(playerOneShips);
        match.calculateAndSetPlayerOneFieldsRemaining();
        match.calculateAndSetPlayerOneShipsRemainingMap();
        match.setPlayerTwoShips(playerTwoShips);
        match.calculateAndSetPlayerTwoFieldsRemaining();
        match.calculateAndSetPlayerTwoShipsRemainingMap();
        LOGGER.trace("Match set as active");
        Random rd = new Random();
        if (rd.nextBoolean()) {
            match.setShootingPlayer(playerOne);
            LOGGER.trace("First shooting player {}", playerOne.getUuid());
        } else {
            match.setShootingPlayer(playerTwo);
            LOGGER.trace("First shooting player {}", playerTwo.getUuid());
        }

        match.setActive(true);
        matchService.saveMatch(match);
        playerOne.addPlayerOneGame(match);
        playerService.savePlayer(playerOne);
        playerTwo.addPlayerTwoGame(match);
        playerService.savePlayer(playerTwo);
        return match;
    }

    private Match initOnePlayerGame(Player playerOne, char[][] playerOneShips) {
        Match match = new Match();
        match.setUuid(UUID.randomUUID());
        match.wipeShootsBoard();
        LOGGER.trace("New game UUID: {}", match.getUuid());
        match.setPlayerOneShips(playerOneShips);
        match.calculateAndSetPlayerOneFieldsRemaining();
        match.calculateAndSetPlayerOneShipsRemainingMap();

        matchService.saveMatch(match);
        playerOne.addPlayerOneGame(match);
        playerService.savePlayer(playerOne);

        return match;
    }

    private void fillPlayerTwo(Match match, Player playerTwo, char[][] playerTwoShips) {
        match.setPlayerTwoShips(playerTwoShips);
        match.calculateAndSetPlayerTwoFieldsRemaining();
        match.calculateAndSetPlayerTwoShipsRemainingMap();
        match.setActive(true);
        LOGGER.trace("Match set as active");
        Random rd = new Random();
        if (rd.nextBoolean()) {
            match.setShootingPlayer(match.getPlayerOne());
            LOGGER.trace("First shooting player {}", match.getPlayerOne().getUuid());
        } else {
            match.setShootingPlayer(playerTwo);
            LOGGER.trace("First shooting player {}", playerTwo.getUuid());
        }
        playerTwo.addPlayerTwoGame(match);
        playerService.savePlayer(playerTwo);
    }

    private void loadWaitingGames() {
        List<Match> matches = matchService.listNotStartedGames();
        for (Match match : matches) {
            waitingMatches.offer(match);
        }
    }

    @AllArgsConstructor
    private static class QueuedPlayer {
        @Getter
        @Setter
        private Player player;
        @Getter
        @Setter
        private char[][] playerShips;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QueuedPlayer)) return false;
            return player != null && player.equals(((QueuedPlayer) o).getPlayer()) && playerShips != null && Arrays.deepEquals(playerShips, ((QueuedPlayer) o).getPlayerShips());
        }

        @Override
        public String toString() {
            return "player [uuid: " + player.getUuid().toString() + " name: " + player.getName() + "]";
        }
    }
}
