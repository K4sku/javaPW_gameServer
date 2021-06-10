package pl.ee.gameServer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.ee.gameServer.GameServer;

import java.util.LinkedList;

@Service
public class ShipValidatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShipValidatorService.class);


    public static boolean validateShipPlacement(char[][] map){
        Character currentChar;
        LinkedList<Coordinate> adjacentFields = new LinkedList<>();
        for(int row = 0; row < GameServer.BOARD_SIZE; row++){
            for(int col = 0; col < GameServer.BOARD_SIZE; col++){
                if(map[row][col] != '0'){
                    currentChar = map[row][col];
                    if (row-1 >= 0 ) {
                        if (col-1 >= 0) adjacentFields.add(new Coordinate(row - 1, col - 1));
                        adjacentFields.add(new Coordinate(row - 1, col));
                        if (col+1 < GameServer.BOARD_SIZE) adjacentFields.add(new Coordinate(row - 1, col + 1));
                    }
                    if (col-1 >= 0) adjacentFields.add(new Coordinate(row, col - 1));
                    if (col+1 < GameServer.BOARD_SIZE) adjacentFields.add(new Coordinate(row, col + 1));
                    if (row+1 < GameServer.BOARD_SIZE) {
                        if (col-1 >= 0) adjacentFields.add(new Coordinate(row + 1, col - 1));
                        adjacentFields.add(new Coordinate(row + 1, col));
                        if (col+1 < GameServer.BOARD_SIZE) adjacentFields.add(new Coordinate(row + 1, col + 1));
                    }
                    LOGGER.debug("Validating for coordinate: {},{}, value: {}", row, col, currentChar);
                    for(Coordinate coordinate : adjacentFields) {
                        LOGGER.debug("Pos: {},{} holds value: {}", coordinate.x, coordinate.y, map[coordinate.x][coordinate.y]);
                        if(map[coordinate.x][coordinate.y] != '0' ){
                            if (!currentChar.equals(map[coordinate.x][coordinate.y])){
                                LOGGER.trace("Ship placement is invalid");
                                return false;
                            }
                        }
                    }
                    adjacentFields.clear();
                }
            }
        }
        LOGGER.trace("Ship placement is valid");
        return true;
    }



}
