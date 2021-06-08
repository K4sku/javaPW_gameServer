package pl.ee.gameServer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.ee.gameServer.GameServer;

@Service
public class BoardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardService.class);


    public static char[][] parseToCharArray(String input) throws Exception {

        if (input.length()== GameServer.BOARD_SIZE * GameServer.BOARD_SIZE) {
            char[][] board = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
            for (int x = 0; x < GameServer.BOARD_SIZE; x++) {
                for (int y = 0; y < GameServer.BOARD_SIZE; y++) {
                    board[x % GameServer.BOARD_SIZE][y] = input.charAt(x * GameServer.BOARD_SIZE + y);
                }
            }
            return board;
        } else {
            LOGGER.debug("Invalid input size");
            throw new Exception("InvalidBoardSizeException");
        }
    }
}
