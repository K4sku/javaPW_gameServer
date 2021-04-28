package pl.ee.gameServer.model;


import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;

@Entity @Data
@Table(name="games")
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String gameID;
//    @Column(nullable = false, length = 100)
//    private Player playerOne;
////    @Column(nullable = false, length = 100)
//    private Player playerTwo;
////    @Column(nullable = false, length = 50)
//    private Player winner;
    @Column
    private boolean isActive = true;
//    @Column
//    private Player playerTurn;
    @Column
    private boolean surrendered = false;
    @Column
    private Instant started;

    @Column
    private char[][] playerOneShots = new char[10][10];
    @Column
    private char[][] playerOneShips = new char[10][10];
    @Column
    private char[][] playerTwoShots = new char[10][10];
    @Column
    private char[][] playerTwoShips = new char[10][10];

}
