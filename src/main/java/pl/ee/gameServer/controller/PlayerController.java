package pl.ee.gameServer.controller;

import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.service.MatchMakerService;
import pl.ee.gameServer.service.PlayerService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/players")
public class PlayerController {
    final
    PlayerService playerService;
    final
    MatchMakerService matchMakerService;

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
    public void add(@RequestBody Player player) {

        playerService.savePlayer(player);
    }

    @PostMapping("/{uuid}")
    public ResponseEntity<?> update(@RequestBody Player player, @PathVariable UUID uuid) {
        try {
//            Player existPlayer = playerService.getPlayer(uuid);
            player.setUuid(uuid);
            playerService.savePlayer(player);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{uuid}/new_game")
    public ResponseEntity<?> newMatchmaking(@RequestBody Match match, @PathVariable UUID uuid) {
        try {
            Player existPlayer = playerService.getPlayer(uuid);
            Hibernate.initialize(existPlayer.getPlayerOneGames());
            matchMakerService.addPlayerToQueue(existPlayer, match);
//            matchMakerService.matchPlayers();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable UUID uuid) {
        playerService.deletePlayer(uuid);
    }
}
