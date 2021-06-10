package pl.ee.gameServer.service;

import javax.validation.constraints.NotNull;

public class Coordinate {
    public int x;
    public int y;

    /**
     * Returns new Coordinate Object
     * @param x - value of x position
     * @param y - value of y position
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Default constructor method, return new Object with x:-1, y:-1
     */
    public Coordinate() {
        this.x = -1;
        this.y = -1;
    }

    /**
     * Parses String input to Coordinate object
     * @param input Accepts String in syntax "x:1,y:1", "x: 1, y: 1", "x : 1, y : 1".
     * @return new Coordinate or null if syntax is invalid.
     */
    public static Coordinate parseString(@NotNull String input){
        int x =-1, y = -1;
        String[] parts = input.split("[:,]");
        //remove whitespace
        for (int i=0; i<parts.length; i++){ parts[i] = parts[i].strip(); }
        for (int i=0; i<parts.length; i++) {
            if (parts[i].equals("x")) x = Integer.parseInt(parts[i + 1]);
            if (parts[i].equals("y")) y = Integer.parseInt(parts[i + 1]);
        }
        if (x != -1 && y != -1) return new Coordinate(x, y);
        return null;
    }

    @Override
    public String toString(){
        return "x: "+x+", y: "+y;
    }
}
