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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class GameLoopService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoopService.class);

    final MatchService matchService;
    final PlayerService playerService;
    private final ConcurrentHashMap<UUID, Match> matchesTurns = new ConcurrentHashMap();

    public GameLoopService(MatchService matchService, PlayerService playerService) {
        this.matchService = matchService;
        this.playerService = playerService;
    }

    /**
     * Verifies and registers shoots taken by players
     * @param matchUuid     uuid of match
     * @param playerUuid    uuid of player that took the shot
     * @param shootLocation coordinate of shoot
     * @return first element of Pair is int code [-1 = error, 0 = missed shot, 1..5 = shipType if shot hit, 10 = show won the game], second element is message send to frontend.
     */
    public Pair<Integer, String> shoot(UUID matchUuid, UUID playerUuid, Coordinate shootLocation) {
        int hit;
        boolean sink = false;
        try {
            Match match = matchService.getMatch(matchUuid);
            matchesTurns.putIfAbsent(matchUuid, match);
            Player shootingPlayer = playerService.getPlayer(playerUuid);
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

            Player opponent = match.getOpponent(shootingPlayer);
            if (wasNotShoot(match.selectPlayersShots(shootingPlayer), shootLocation)) {
                hit = isHit(match.selectPlayersShips(opponent), shootLocation);
                LOGGER.trace("isHit returned: {}", hit);
                if (0 < hit && hit < 6) {
                    sink = isSunk(match.selectPlayersShipsRemainingMap(opponent), hit);
                    LOGGER.trace("isSunk returned {}", sink);
                }
                registerPlayerShot(match, shootingPlayer, shootLocation, hit, sink);
                LOGGER.trace("It's playerOne shoot at {} shot landed on {}, sink: {}", shootLocation, hit, sink);
                notifyAll();

                if (checkWinCondition(match)) return Pair.of(10, "Game over");
                if (hit != 0 && hit != -1) return Pair.of(hit, "HIT");
                return Pair.of(0, "MISS");
            } else {
                LOGGER.trace("Player already shoot at field {}", shootLocation);
                return Pair.of(-1, "Player already shoot at that field");
            }

        } catch (Exception e) {
            LOGGER.debug(String.valueOf(e));
        }
        return null;
    }

    public boolean isPlayerInMatch(Match match, Player player) {
        return match.getPlayersList().contains(player);
    }

    public boolean isPlayerTurn(Match match, Player player) {
        return  match.getShootingPlayer().equals(player);
    }

    public boolean waitForTurn(Match match, Player player) throws InterruptedException {
        matchesTurns.putIfAbsent(match.getUuid(), match);
        //check if it's this player turn
        while (!matchesTurns.get(match.getUuid()).getShootingPlayer().equals(player)){
            wait();
        }
        if (match.getShootingPlayer().equals(player)) {
            LOGGER.trace("It's not player: {} turn yet.", player.getUuid());
            return true;
        }
        return false;
    }

    /**
     * Validates that shot was inside game board bounds
     * @param shot coordinate of shot
     * @return true if shot coordinates are valid, otherwise false
     */
    private boolean isInValidRange(Coordinate shot) {
        return (shot.x >= 0 && shot.x <= (GameServer.BOARD_SIZE - 1)) && (shot.y >= 0 && shot.y <= (GameServer.BOARD_SIZE - 1));
    }

    /**
     * Checks if coordinates were not shot before
     * @param board board to validate
     * @param shot  coordinate of shot
     * @return true if there is no shot, otherwise false
     */
    private boolean wasNotShoot(char[][] board, Coordinate shot) {
        return board[shot.y][shot.x] == '0';
    }

    /**
     * Validates hit
     * @param board board to validate
     * @param shot  coordinate of shot
     * @return true if ship was hit, otherwise false
     */
    private int isHit(char[][] board, Coordinate shot) {
        return Character.getNumericValue(board[shot.y][shot.x]);
    }

    /**
     * Checks if shot sunk the ship (all ship fields were hit)
     * @param playerRemainingShipMap map of ship fields not hit
     * @param shipType               type of ship that was hit
     * @return false if last field of ship was hit
     */
    private boolean isSunk(Map<Character, Integer> playerRemainingShipMap, int shipType) {
        LOGGER.debug("isSunk: \n" +
                        "playerRemainingShipMap: {} \n" +
                        "shipType: {}",
                playerRemainingShipMap, shipType);
        return (playerRemainingShipMap.get(Character.forDigit(shipType, 10)) == 1);
    }

    /**
     * Register player shot, updates match model and changes turn
     * @param match match instance
     * @param shootingPlayer shooting player instance
     * @param shot shot coordinate
     * @param hit shipType
     * @param sunk if last shot sunk a shipType ship
     */
    private void registerPlayerShot(Match match, Player shootingPlayer, Coordinate shot, int hit, boolean sunk) {
        match.setLastShot(shot);
        match.setLastShotHit(hit);
        match.setLastShotSunk(sunk);
        char[][] playerShots = match.selectPlayersShots(shootingPlayer);
        playerShots[shot.y][shot.x] = '1';
        match.setPlayersShots(shootingPlayer, playerShots);
        if (hit != 0 && hit != -1) match.registerHit(shootingPlayer, hit);
        match.setOpponentsTurn(shootingPlayer);
        matchService.saveMatch(match);
    }

    /**
     * Checks if game was surrendered or win game conditions were met
     * @param match match instance to check
     * @return true if game is won, otherwise false
     */
    private static boolean checkWinCondition(Match match) {
        if (match.isSurrendered()) return true;
        if (match.getPlayerOneFieldsRemainingCount() == 0) {
            match.setWinnerPlayer(match.getPlayerTwo());
            match.getPlayerOne().updateScore(false);
            match.getPlayerTwo().updateScore(true);
            return true;
        }
        if (match.getPlayerTwoFieldsRemainingCount() == 0) {
            match.setWinnerPlayer(match.getPlayerOne());
            match.getPlayerOne().updateScore(true);
            match.getPlayerTwo().updateScore(false);
            return true;
        }
        return false;
    }

    /**
     * Set's player winner and surrender flag
     * @param match match instance
     * @param playerUuid player that surrendered game
     */
    public void surrender(Match match, UUID playerUuid) {
        try {
            Player player = playerService.getPlayer(playerUuid);
            match.setSurrendered(true);
            List<Player> matchPlayersList = match.getPlayersList();
            matchPlayersList.remove(player);
            match.setWinnerPlayer(matchPlayersList.get(0));
        } catch (Exception e) {
            LOGGER.debug(String.valueOf(e));
        }
    }
}
