package pl.ee.gameServer.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.model.Views;
import pl.ee.gameServer.service.MatchMakerService;
import pl.ee.gameServer.service.PlayerService;

import java.util.List;
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

    @JsonView(Views.Public.class)
    @GetMapping("")
    public List<Player> list() {
        return playerService.listAllPlayer();
    }

    @JsonView(Views.Public.class)
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
    @JsonView(Views.Private.class)
    public ResponseEntity<Player> add(@RequestBody Player player) {
        LOGGER.trace("POST: players/" + player.toString());
        player.setUuid(UUID.randomUUID());
        try {
            playerService.savePlayer(player);
            return new ResponseEntity<>(player, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

//    @JsonView(Views.Private.class)
//    @PostMapping("/{uuid}")
//    public ResponseEntity<?> update(@RequestBody Player player, @PathVariable UUID uuid) {
//        try {
//            player.setUuid(uuid);
//            playerService.savePlayer(player);
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (NoSuchElementException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }

//    @DeleteMapping("/{uuid}")
//    public void delete(@PathVariable UUID uuid) {
//        playerService.deletePlayer(uuid);
//    }
}
