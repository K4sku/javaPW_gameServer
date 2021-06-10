package pl.ee.gameServer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ee.gameServer.GameServer;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;

import java.util.List;
import java.util.UUID;
@Service
@Transactional
public class GameLoopService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoopService.class);

    final MatchService matchService;
    final PlayerService playerService;

    public GameLoopService(MatchService matchService, PlayerService playerService) {
        this.matchService = matchService;
        this.playerService = playerService;
    }

    public Pair<Integer, String> shoot(UUID matchUuid, UUID playerUuid, Coordinate shootLocation) {
        boolean playerOneShooting;
        boolean hit;
        try {
            Match match = matchService.getMatch(matchUuid); //check if game exist
            Player shootingPlayer = playerService.getPlayer(playerUuid); //check if player exist
            //check if player is in game
            if (!match.getPlayersList().contains(shootingPlayer)) return Pair.of(-1, "Player is not in game");
            LOGGER.trace("player is in match");

            //check if it's this player turn
            if (!match.getShootingPlayer().equals(shootingPlayer)) {
                LOGGER.trace("It's not player: {} turn, wait for player: {} to shot first", shootingPlayer.getUuid(), match.getShootingPlayer().getUuid());
                return Pair.of(-1, "It's not player turn");
            }
            LOGGER.trace("It's player: {} turn to shoot", playerUuid);

            //check if shoot is in valid range
            if (!isInValidRange(shootLocation)) {
                LOGGER.trace("Shoot is outside of board");
                return Pair.of(-1, "Shoot is outside of board");
            }

            //check if shoot is not duplicated
            playerOneShooting = match.getPlayerOne().equals(shootingPlayer);
            if (playerOneShooting) {
                if (wasNotShoot(match.getPlayerOneShots(), shootLocation)) {
                    hit = isHit(match.getPlayerTwoShips(), shootLocation);
                    registerPlayerOneShot(match, shootLocation);
                    LOGGER.trace("It's playerOne shoot at {} shot was {}", shootLocation, hit);

                    if(checkWinCondition(match)) return Pair.of(10, "Game over");
                    if (hit) return Pair.of(1, "HIT");
                    return Pair.of(2, "MISS");
                } else {
                    LOGGER.trace("PlayerOne already shoot at field {}", shootLocation);
                    return Pair.of(-1, "Player already shoot at that field");
                }
            } else {
                if (wasNotShoot(match.getPlayerTwoShots(), shootLocation)) {
                    hit = isHit(match.getPlayerOneShips(), shootLocation);
                    registerPlayerTwoShot(match, shootLocation);
                    LOGGER.trace("It's playerTwo shoot at {} shot was {}", shootLocation, hit);

                    if(checkWinCondition(match)) return Pair.of(10, "Game over");
                    if (hit) return Pair.of(1, "HIT");
                    return Pair.of(2, "MISS");
                } else {
                    LOGGER.trace("PlayerTwo already shoot at field {}", shootLocation);
                    return Pair.of(-1, "Player already shoot at that field");
                }
            }
        } catch (Exception e) {
            LOGGER.debug(String.valueOf(e));
        }
        return null;
    }

    private boolean isInValidRange(Coordinate shot) {
        return (shot.x >= 0 && shot.x <= (GameServer.BOARD_SIZE - 1)) && (shot.y >= 0 && shot.y <= (GameServer.BOARD_SIZE - 1));
    }

    private boolean wasNotShoot(char[][] board, Coordinate shot) {
        return board[shot.x][shot.y] == '0';
    }

    private boolean isHit(char[][] board, Coordinate shot) {
        return board[shot.x][shot.y] != '0';
    }

    private void registerPlayerOneShot(Match match, Coordinate shot) {
        match.setLastShot(shot);
        char[][] playerOneShots = match.getPlayerOneShots();
        playerOneShots[shot.y][shot.x] = '1';
        match.setPlayerOneShots(playerOneShots);
        match.decrementPlayerTwoFieldsRemaining();
        match.setShootingPlayer(match.getPlayerTwo());
        matchService.saveMatch(match);
    }

    private void registerPlayerTwoShot(Match match, Coordinate shot) {
        match.setLastShot(shot);
        var playerTwoShots = match.getPlayerTwoShots();
        playerTwoShots[shot.y][shot.x] = '1';
        match.setPlayerOneShots(playerTwoShots);
        match.decrementPlayerOneFieldsRemaining();
        match.setShootingPlayer(match.getPlayerOne());
        matchService.saveMatch(match);
    }

    private static boolean checkWinCondition(Match match) {
        if (match.isSurrendered()) return true;
        if (match.getPlayerOneFieldsRemaining() == 0) {
            match.setWinnerPlayer(match.getPlayerTwo());
            return true;
        }
        if (match.getPlayerTwoFieldsRemaining() == 0) {
            match.setWinnerPlayer(match.getPlayerOne());
            return true;
        }
        return false;
    }

    public void surrender(Match match, UUID playerUuid) {
        try {
            Player player = playerService.getPlayer(playerUuid); //check if player exist
            match.setSurrendered(true);
            List<Player> matchPlayersList = match.getPlayersList();
            matchPlayersList.remove(player);
            match.setWinnerPlayer(matchPlayersList.get(0));
        } catch (Exception e) {
            LOGGER.debug(String.valueOf(e));
        }
    }
}
