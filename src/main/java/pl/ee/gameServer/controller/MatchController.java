package pl.ee.gameServer.controller;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.service.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
public class MatchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchController.class);

    final MatchService matchService;
    final MatchMakerService matchMakerService;
    final GameLoopService gameLoopService;

    public MatchController(MatchService matchService, GameLoopService gameLoopService, MatchMakerService matchMakerService) {
        this.matchService = matchService;
        this.matchMakerService = matchMakerService;
        this.gameLoopService = gameLoopService;
    }

    @GetMapping("")
    public List<Match> list(){
        return matchService.listAllMatch();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Match> get(@PathVariable UUID uuid){
        try {
            Match match = matchService.getMatch(uuid);
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

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

    @PostMapping("/new_game")
    public ResponseEntity<Match> newMatchmaking(@RequestBody Map<String, Object> body) {
        Match match;
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            LOGGER.info("Received new match request for player uuid: {}", playerUuid);
            char[][] board = BoardService.parseToCharArray((String) body.get("board"));
            matchMakerService.addPlayerToQueue(playerUuid, board);
            match = matchMakerService.matchPlayers();
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.debug(e.toString());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @PostMapping("/{uuid}/shoot")
    public ResponseEntity<?> shoot(@RequestBody Map<String, Object> body, @PathVariable UUID uuid){
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            Coordinate shootCoordinate = new Coordinate((Integer) body.get("x"),(Integer) body.get("y"));
            LOGGER.trace("POST: PATH: /matches/{}/shoot | Player: {} | Coordinate: {}", uuid, playerUuid, shootCoordinate);
            var response = gameLoopService.shoot(uuid, playerUuid, shootCoordinate);
            if(response.getFirst()!= -1) {
                Match match = matchService.getMatch(uuid);
                return new ResponseEntity<>(match, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response.getSecond(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/{uuid}/surrender")
    public ResponseEntity<?> surrender(@RequestBody Map<String, Object> body, @PathVariable UUID uuid) {
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            Match match = matchService.getMatch(uuid);
            gameLoopService.surrender(match, playerUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable UUID uuid){
        matchService.deleteMatch(uuid);
    }

}
