package pl.ee.gameServer.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.model.Views;
import pl.ee.gameServer.service.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

@RestController
@RequestMapping("/matches")
public class MatchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchController.class);

    final MatchService matchService;
    final PlayerService playerService;
    final MatchMakerService matchMakerService;
    final GameLoopService gameLoopService;

    public MatchController(MatchService matchService, GameLoopService gameLoopService, MatchMakerService matchMakerService, PlayerService playerService) {
        this.matchService = matchService;
        this.matchMakerService = matchMakerService;
        this.gameLoopService = gameLoopService;
        this.playerService = playerService;
    }

    @JsonView(Views.Public.class)
    @GetMapping("")
    public List<Match> list() {
        return matchService.listAllMatch();
    }

    @JsonView(Views.Public.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<Match> get(@PathVariable UUID uuid) {
        try {
            Match match = matchService.getMatch(uuid);
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @JsonView(Views.Public.class)
    @PostMapping("/{uuid}")
    public ResponseEntity<?> update(@RequestBody Match match, @PathVariable UUID uuid) {
        try {
            match.setUuid(uuid);
            matchService.saveMatch(match);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @JsonView(Views.Public.class)
    @PostMapping("/new_game")
    public ResponseEntity<Match> newMatchmaking(@RequestBody Map<String, Object> body) {
        Match match;
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            UUID privateToken = UUID.fromString((String) body.get("privateToken"));
            if (playerService.isPrivateTokenValid(playerUuid, privateToken)) {
                LOGGER.info("Received new match request for player uuid: {}", playerUuid);
                char[][] board = BoardService.parseToCharArray((String) body.get("board"));
                matchMakerService.addPlayerToQueue(playerUuid, board);
                match = matchMakerService.matchPlayers();
                return new ResponseEntity<>(match, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @JsonView(Views.Public.class)
    @PostMapping("/{uuid}/shoot")
    public ResponseEntity<?> shoot(@RequestBody Map<String, Object> body, @PathVariable UUID uuid) {
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            UUID privateToken = UUID.fromString((String) body.get("privateToken"));
            Coordinate shootCoordinate = new Coordinate((Integer) body.get("x"), (Integer) body.get("y"));
            if (playerService.isPrivateTokenValid(playerUuid, privateToken)) {
                LOGGER.trace("POST: PATH: /matches/{}/shoot | Player: {} | Coordinate: {}", uuid, playerUuid, shootCoordinate);
                Pair<Integer, String> response = gameLoopService.shoot(uuid, playerUuid, shootCoordinate);
                if (response.getFirst() != -1) {
                    Match match = matchService.getMatch(uuid);
                    return new ResponseEntity<>(match, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response.getSecond(), HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //TODO finish this one
    @JsonView(Views.Public.class)
    @GetMapping("/{matchUuid}/turn")
    public DeferredResult<?> checkTurn(@RequestBody Map<String, Object> body, @PathVariable UUID matchUuid) {
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
        output.onCompletion(() ->
                output.setResult(ResponseEntity.ok("ok")));
        output.onTimeout(() ->
                output.setErrorResult(
                        ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                        .body("Request timeout occured")
                ));
        ForkJoinPool.commonPool().submit(() -> {
            LOGGER.info("Processing in separate thread");
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
            }
            output.setResult(ResponseEntity.ok("ok"));
        });
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            UUID privateToken = UUID.fromString((String) body.get("privateToken"));
            Match match = matchService.getMatch(matchUuid);
            Player player = playerService.getPlayer(playerUuid);
            if (playerService.isPrivateTokenValid(playerUuid, privateToken) && gameLoopService.isPlayerInMatch(match, player)) {
                gameLoopService.waitForTurn(match, player);
            } else {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }

//        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @JsonView(Views.Public.class)
    @PostMapping("/{uuid}/surrender")
    public ResponseEntity<?> surrender(@RequestBody Map<String, Object> body, @PathVariable UUID uuid) {
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            UUID privateToken = UUID.fromString((String) body.get("privateToken"));
            if (playerService.isPrivateTokenValid(playerUuid, privateToken)) {
                Match match = matchService.getMatch(uuid);
                gameLoopService.surrender(match, playerUuid);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @JsonView(Views.Public.class)
    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable UUID uuid) {
        matchService.deleteMatch(uuid);
    }

}
