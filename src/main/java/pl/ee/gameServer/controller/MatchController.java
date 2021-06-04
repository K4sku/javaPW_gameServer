package pl.ee.gameServer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.service.MatchService;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/matches")
public class MatchController {
    final
    MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("")
    public List<Match> list(){
        return matchService.listAllMatch();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> get(@PathVariable Integer id){
        try {
            Match match = matchService.getMatch(id);
            return new ResponseEntity<>(match, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody Match match, @PathVariable Integer id) {
        try {
            match.setId(id);
            matchService.saveMatch(match);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        matchService.deleteMatch(id);
    }

}
