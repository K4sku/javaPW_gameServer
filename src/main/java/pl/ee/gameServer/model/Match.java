package pl.ee.gameServer.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity @Data
@Table(name="games")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private boolean surrendered = false;
    @Column
    private Instant started;
//    @Column
//    private char[][] playerOneShots = new char[10][10];
//    @Column
//    private char[][] playerOneShips = new char[10][10];
//    @Column
//    private char[][] playerTwoShots = new char[10][10];
//    @Column
//    private char[][] playerTwoShips = new char[10][10];
    @Column(length = 100)
    private String winner;
    @Column
    private boolean isActive = true;


    @JsonIgnoreProperties("matches")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "matches")
    public Set<Player> players = new HashSet<>();

    public void addPlayers(Player[] players) {
        this.players.addAll(Arrays.asList(players));
        for (Player player : players ) {
            player.getMatches().add(this);
        }
    }

}
