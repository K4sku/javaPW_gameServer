package pl.ee.gameServer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.service.Coordinate;
import pl.ee.gameServer.service.GameLoopService;
import pl.ee.gameServer.service.MatchService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
public class MatchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchController.class);

    final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
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

    @PostMapping("/{uuid}/shoot")
    public ResponseEntity<?> shoot(@RequestBody Map<String, Object> body, @PathVariable UUID uuid){
        try {
            UUID playerUuid = UUID.fromString((String) body.get("player-uuid"));
            Coordinate shootCoordinate = new Coordinate(Integer.parseInt((String) body.get("x")), Integer.parseInt((String) body.get("y")));

        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable UUID uuid){
        matchService.deleteMatch(uuid);
    }

}
