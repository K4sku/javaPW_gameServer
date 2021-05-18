package pl.ee.gameServer.service;

import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class ShipValidatorService {
    public static boolean validateShipPlacement(char[][] map){
        int rowCount = map[0].length;
        int colCount = map.length;
        for(int row = 0; row < rowCount; row++){
            for(int col = 0; col < colCount; col++){
                if(Character.isLetterOrDigit(map[row][col])){
                    Character currentChar = map[row][col];
                    LinkedList<Coordinate> adjecentFields = new LinkedList<>();
                    adjecentFields.add(new Coordinate(row - 1, col - 1));
                    adjecentFields.add(new Coordinate(row - 1, col));
                    adjecentFields.add(new Coordinate(row - 1, col + 1));
                    adjecentFields.add(new Coordinate(row, col - 1));
                    adjecentFields.add(new Coordinate(row, col + 1));
                    adjecentFields.add(new Coordinate(row + 1, col - 1));
                    adjecentFields.add(new Coordinate(row + 1, col));
                    adjecentFields.add(new Coordinate(row + 1, col + 1));

                    for(Coordinate coordinate : adjecentFields) {
                        if(!isInValidRange(coordinate.x, coordinate.y, rowCount, colCount)){
                          if(Character.isLetterOrDigit(map[coordinate.x][coordinate.y])){
                                if(!currentChar.equals(map[coordinate.x][coordinate.y])){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isInValidRange(int x, int y, int rowCount, int colCount){
        //sprawdzanie poza zakresem
        return x < 0 || x > (rowCount-1) || y < 0 || y > (colCount-1);
    }

    private static class Coordinate {
        int x;
        int y;
        private Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

}
