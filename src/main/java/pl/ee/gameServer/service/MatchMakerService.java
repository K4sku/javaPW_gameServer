package pl.ee.gameServer.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.MatchRepository;
import pl.ee.gameServer.repository.PlayerRepository;


import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Transactional
public class MatchMakerService {
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchMakerService.class);

    private final LinkedBlockingQueue<QueuedPlayer> waitingPlayers = new LinkedBlockingQueue<>(100);
    private final LinkedBlockingQueue<Match> waitingMatches = new LinkedBlockingQueue<>(100);

    public MatchMakerService(MatchRepository matchRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        loadWaitingGames();
    }

    public void addPlayerToQueue(Player player, char[][] board)
    {
        LOGGER.info("Adding player {} : {} to matchmaking queue. Players waiting in queue: {}", player.getName(), player.getUuid(), waitingPlayers.size()+1);
        QueuedPlayer queuedPlayer = new QueuedPlayer(player, board);
        waitingPlayers.offer(queuedPlayer);
    }

    public Match matchPlayers(){
        QueuedPlayer playerOne = waitingPlayers.poll(); //take p1 from queue head
        if (playerOne != null) {
            LOGGER.trace("Player one from queue {}", playerOne.getPlayer().getUuid());
            QueuedPlayer playerTwo = waitingPlayers.peek(); //get p2, but leave on queue head
            if (playerTwo != null) {
                LOGGER.trace("Player two from queue {}", playerTwo.getPlayer().getUuid());
                if (playerOne.getPlayer().equals(playerTwo.getPlayer())) {
                    LOGGER.trace("PlayerOne and PlayerTwo are the same, putting player one at the end of queue");
                    waitingPlayers.offer(playerOne); // put p1 on back of queue
                    return matchPlayers();
                } else {
                    //take p2 from queue and make game with p1 and p2
                    LOGGER.trace("Two different players. Starting new match.");
                    waitingPlayers.poll();
                    return initGame(playerOne.getPlayer(), playerOne.getPlayerShips(), playerTwo.getPlayer(), playerTwo.getPlayerShips());
                }
            } else if (waitingMatches.size() >0){ //if there are matches waiting for 2nd player
                LOGGER.trace("No playerTwo to match, check waiting games.");
                int matchQueueSize = waitingMatches.size();
                Match waitingMatch;
                for (int i =0; i < matchQueueSize; i++){ //iterate over queue
                    waitingMatch = waitingMatches.poll();
                    if (waitingMatch != null) { //avoid NullPointerException
                        LOGGER.trace("There is game waiting matchUUID: {} | playerOne uuid: {}", waitingMatch.getUuid(), waitingMatch.getPlayerOne().getUuid());
                        if (waitingMatch.getPlayerOne().equals(playerOne.getPlayer())) { //avoid matching player with himself
                            LOGGER.trace("Player already in game");
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
                LOGGER.trace("Start game with one player and add it to the queue.");
                Match match = initOnePlayerGame(playerOne.getPlayer(), playerOne.getPlayerShips());
                waitingMatches.offer(match);
                return match;
            }
        }
        return null;
    }

    public Match initGame(Player playerOne, char[][] playerOneShips, Player playerTwo, char[][] playerTwoShips) {
        Match match = new Match();
        match.wipeShootsBoard();
        match.setUuid(UUID.randomUUID());
        LOGGER.trace("New game UUID: {}", match.getUuid());
        match.setPlayerOneShips(playerOneShips);
        match.calculateAndSetPlayerOneFieldsRemaining();
        match.setPlayerTwoShips(playerTwoShips);
        match.calculateAndSetPlayerTwoFieldsRemaining();
        match.setActive(true);
        LOGGER.trace("Match set as active");
        Random rd = new Random();
        if (rd.nextBoolean()) {
            match.setShootingPlayer(playerOne);
            LOGGER.trace("First shooting player {}", playerOne.getUuid());
        } else {
            match.setShootingPlayer(playerTwo);
            LOGGER.trace("First shooting player {}", playerTwo.getUuid());
        }

        matchRepository.save(match);
        playerOne.addPlayerOneGame(match);
        playerRepository.save(playerOne);
        playerTwo.addPlayerTwoGame(match);
        playerRepository.save(playerTwo);
        return match;
    }

    public Match initOnePlayerGame(Player playerOne, char[][] playerOneShips){
        Match match = new Match();
        match.wipeShootsBoard();
        match.setUuid(UUID.randomUUID());
        LOGGER.trace("New game UUID: {}", match.getUuid());
        match.setPlayerOneShips(playerOneShips);
        match.calculateAndSetPlayerOneFieldsRemaining();

//        Player mockP2 = new Player();
//        mockP2.setUuid(UUID.randomUUID());
//        mockP2.setName("Testus");
//        match.setPlayerTwo(mockP2);
//        LOGGER.debug("Adding mock player Testus with uuid {}", mockP2.getUuid());
//        playerRepository.save(mockP2);
//        match.setPlayerTwoShips(playerOneShips);
//        match.setActive(true);
//        match.setShootingPlayer(playerOne);
//        LOGGER.trace("Shooting player {}", playerOne.getUuid());

        matchRepository.save(match);
        playerOne.addPlayerOneGame(match);
        playerRepository.save(playerOne);

//        mockP2.addPlayerTwoGame(match);
//        playerRepository.save(mockP2);
        return match;
    }

    public void fillPlayerTwo(Match match, Player playerTwo, char[][] playerTwoShips){
        match.setPlayerTwoShips(playerTwoShips);
        match.calculateAndSetPlayerTwoFieldsRemaining();
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
        playerRepository.save(playerTwo);
    }

    private void loadWaitingGames(){
        List<Match> matches = matchRepository.findNotStartedGames();
        for (Match match: matches) {
            waitingMatches.offer(match);
        }
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
