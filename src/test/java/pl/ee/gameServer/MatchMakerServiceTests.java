package pl.ee.gameServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ee.gameServer.model.Match;
import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.MatchRepository;
import pl.ee.gameServer.repository.PlayerRepository;
import pl.ee.gameServer.service.MatchMakerService;

import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchMakerServiceTests {

    Player playerOne;
    char[][] playerOneBoard;
    Player playerTwo;
    char[][] playerTwoBoard;
    HashMap<Character, Integer> newMatchShipsRemainingMap;
    char[][] emptyShots;
    @Mock
    MatchRepository mockedMatchRepository;
    @Mock
    PlayerRepository mockedPlayerRepository;
    @InjectMocks
    MatchMakerService matchMakerService;

    @BeforeEach
    public void setUp() {
        playerOne = new Player();
        playerOne.setName("playerOne");
        playerOne.setUuid(UUID.randomUUID());
        playerOneBoard = new char[][]{
                {'1', '1', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '5', '5', '5', '5', '5', '0', '0', '0', '0'},
                {'0', '0', '0', '2', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '2', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '2', '0', '0', '3', '3', '3', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '4'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '4'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '4'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '4'}
        };


        playerTwo = new Player();
        playerTwo.setName("playerTwo");
        playerTwo.setUuid(UUID.randomUUID());
        playerTwoBoard = new char[][]{
                {'0', '0', '0', '0', '0', '0', '0', '1', '1', '0'},
                {'0', '5', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '5', '0', '0', '0', '4', '4', '4', '4', '0'},
                {'0', '5', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '5', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '5', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '2', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '2', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '2', '0', '3', '3', '3', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'}
        };

        emptyShots = new char[][]{
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0'}
        };

        matchMakerService = new MatchMakerService(mockedMatchRepository, mockedPlayerRepository);
        newMatchShipsRemainingMap = new HashMap<>(5);
        newMatchShipsRemainingMap.put('1',2);
        newMatchShipsRemainingMap.put('2',3);
        newMatchShipsRemainingMap.put('3',3);
        newMatchShipsRemainingMap.put('4',4);
        newMatchShipsRemainingMap.put('5',5);
    }

    @Test
    public void shouldStartNewGameIfAddingTwoPlayers() {
        matchMakerService.addPlayerToQueue(playerOne, playerOneBoard);
        matchMakerService.addPlayerToQueue(playerTwo, playerTwoBoard);
        Match match = matchMakerService.matchPlayers();

        verify(mockedMatchRepository, times(1)).save(any(Match.class));
        verify(mockedPlayerRepository, times(2)).save(any(Player.class));

        assertThat(match).isInstanceOf(Match.class);
        assertThat(match.getPlayerOne()).isEqualTo(playerOne);
        assertThat(match.getPlayerOneShips()).isDeepEqualTo(playerOneBoard);
        assertThat(match.getPlayerOneShots()).isDeepEqualTo(emptyShots);
        assertThat(match.getPlayerOneFieldsRemainingCount()).isEqualTo(17); // 17 is number of non empty ships in new game
        assertThat(match.getPlayerOneShipsRemainingMap()).isEqualTo(newMatchShipsRemainingMap);

        assertThat(match.getPlayerTwo()).isEqualTo(playerTwo);
        assertThat(match.getPlayerTwoShips()).isDeepEqualTo(playerTwoBoard);
        assertThat(match.getPlayerTwoShots()).isDeepEqualTo(emptyShots);
        assertThat(match.getPlayerTwoFieldsRemainingCount()).isEqualTo(17); // 17 is number of non empty ships in new game
        assertThat(match.getPlayerTwoShipsRemainingMap()).isEqualTo(newMatchShipsRemainingMap);
        assertThat(match.isActive()).isTrue();
    }

    @Test
    public void shouldNotMatchPlayerWithHimself() {
        matchMakerService.addPlayerToQueue(playerOne, playerOneBoard);
        matchMakerService.addPlayerToQueue(playerOne, playerOneBoard);
        Match match = matchMakerService.matchPlayers();

        assertThat(match).isInstanceOf(Match.class);
        assertThat(match.getPlayerOne()).isEqualTo(playerOne);
        assertThat(match.getPlayerOneShips()).isDeepEqualTo(playerOneBoard);
        assertThat(match.getPlayerOneShots()).isDeepEqualTo(emptyShots);
        assertThat(match.getPlayerOneFieldsRemainingCount()).isEqualTo(17); // 17 is number of non empty ships in new game
        assertThat(match.getPlayerOneShipsRemainingMap()).isEqualTo(newMatchShipsRemainingMap);
        assertThat(match.getPlayerTwo()).isNull();
        assertThat(match.isActive()).isFalse();
    }

    @Test
    public void shouldStartNewGameIfAddingOnePlayer() {
        matchMakerService.addPlayerToQueue(playerOne, playerOneBoard);
        Match match = matchMakerService.matchPlayers();
        verify(mockedMatchRepository, times(1)).save(any(Match.class));
        verify(mockedPlayerRepository, times(1)).save(any(Player.class));

        assertThat(match).isInstanceOf(Match.class);
        assertThat(match.getPlayerOne()).isEqualTo(playerOne);
        assertThat(match.getPlayerOneShips()).isDeepEqualTo(playerOneBoard);
        assertThat(match.getPlayerOneShots()).isDeepEqualTo(emptyShots);
        assertThat(match.getPlayerOneFieldsRemainingCount()).isEqualTo(17); // 17 is number of non empty ships in new game
        assertThat(match.getPlayerOneShipsRemainingMap()).isEqualTo(newMatchShipsRemainingMap);
        assertThat(match.getPlayerTwo()).isNull();
        assertThat(match.isActive()).isFalse();
    }

    @Test
    public void shouldFillGameWithSecondPlayer() {
        matchMakerService.addPlayerToQueue(playerOne, playerOneBoard);
        Match match = matchMakerService.matchPlayers();
        matchMakerService.addPlayerToQueue(playerTwo, playerTwoBoard);
        Match matchTwo = matchMakerService.matchPlayers();
        assertThat(match).isInstanceOf(Match.class);
        assertThat(match).isEqualTo(matchTwo);
        assertThat(match.getPlayerOne()).isEqualTo(playerOne);
        assertThat(match.getPlayerOneShips()).isDeepEqualTo(playerOneBoard);
        assertThat(match.getPlayerOneShots()).isDeepEqualTo(emptyShots);
        assertThat(match.getPlayerOneFieldsRemainingCount()).isEqualTo(17); // 17 is number of non empty ships in new game
        assertThat(match.getPlayerOneShipsRemainingMap()).isEqualTo(newMatchShipsRemainingMap);

        assertThat(match.getPlayerTwo()).isEqualTo(playerTwo);
        assertThat(match.getPlayerTwoShips()).isDeepEqualTo(playerTwoBoard);
        assertThat(match.getPlayerTwoShots()).isDeepEqualTo(emptyShots);
        assertThat(match.getPlayerTwoFieldsRemainingCount()).isEqualTo(17); // 17 is number of non empty ships in new game
        assertThat(match.getPlayerTwoShipsRemainingMap()).isEqualTo(newMatchShipsRemainingMap);
        assertThat(match.isActive()).isTrue();

        verify(mockedMatchRepository, times(1)).save(any(Match.class));
        verify(mockedPlayerRepository, times(2)).save(any(Player.class));
    }
}
