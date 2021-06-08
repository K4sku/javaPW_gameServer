package pl.ee.gameServer.service;

import org.springframework.stereotype.Service;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;

import java.util.UUID;
@Service
public class GameLoopService {
    final MatchService matchService;
    final PlayerService playerService;

    public GameLoopService(MatchService matchService, PlayerService playerService) {
        this.matchService = matchService;
        this.playerService = playerService;
    }



    public boolean shoot(UUID matchUuid, UUID playerUuid, Coordinate shootLocation){
        try{
            Match match = matchService.getMatch(matchUuid);
            Player shootingPlayer = playerService.getPlayer(playerUuid);
        } catch (Exception e) {

        }
        return true;
    }
}
