package pl.ee.gameServer.model;


import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity @Data
@Table(name="games")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String gameID;
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
    @Column(length = 100)
    private String winner;
    @Column
    private boolean isActive = true;

    @ManyToMany(mappedBy = "matches")
    private Set<Player> players = new HashSet<>();


}
