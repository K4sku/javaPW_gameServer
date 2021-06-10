package pl.ee.gameServer.controller;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.service.BoardService;
import pl.ee.gameServer.service.MatchMakerService;
import pl.ee.gameServer.service.PlayerService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/players")
public class PlayerController {
    final PlayerService playerService;
    final MatchMakerService matchMakerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerController.class);

    public PlayerController(PlayerService playerService, MatchMakerService matchMakerService) {
        this.playerService = playerService;
        this.matchMakerService = matchMakerService;
    }

    @GetMapping("")
    public List<Player> list() {
        return playerService.listAllPlayer();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Player> get(@PathVariable UUID uuid) {
        try {
            Player player = playerService.getPlayer(uuid);
            return new ResponseEntity<>(player, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/")
    public ResponseEntity<Player> add(@RequestBody Player player) {
        player.setUuid(UUID.randomUUID());
        try {
            playerService.savePlayer(player);
            return new ResponseEntity<>(player, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/{uuid}")
    public ResponseEntity<?> update(@RequestBody Player player, @PathVariable UUID uuid) {
        try {
            player.setUuid(uuid);
            playerService.savePlayer(player);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{uuid}/new_game")
    public ResponseEntity<Match> newMatchmaking(@RequestBody Map<String, Object> body, @PathVariable UUID uuid) {
        LOGGER.info("Received new match request for player uuid: {}", uuid);
        LOGGER.debug("Post body: {}", body.toString());
        Match match;
        try {
            char[][] board = BoardService.parseToCharArray((String) body.get("board"));
            Player existPlayer = playerService.getPlayer(uuid);
            LOGGER.debug("Player found for uuid, player name {}", existPlayer.getName());
            Hibernate.initialize(existPlayer.getPlayerOneGames());
            matchMakerService.addPlayerToQueue(existPlayer, board);
            match = matchMakerService.matchPlayers();
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.debug(e.toString());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable UUID uuid) {
        playerService.deletePlayer(uuid);
    }
}
