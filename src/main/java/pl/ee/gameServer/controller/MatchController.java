package pl.ee.gameServer.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    MatchService matchService;

    @GetMapping("")
    public List<Match> list(){
        return matchService.listAllMatch();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> get(@PathVariable Integer id){
        try {
            Match match = matchService.getMatch(id);
            return new ResponseEntity<Match>(match, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<Match>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        matchService.deleteMatch(id);
    }

}
