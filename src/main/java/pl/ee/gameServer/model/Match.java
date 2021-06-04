package pl.ee.gameServer.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity @Data
@Table(name="games")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private boolean surrendered = false;
    @CreationTimestamp
    private LocalDateTime createDateTime;
    @UpdateTimestamp
    private LocalDateTime updateDateTime;

//    @Column
//    @OneToMany
//    private Player playerOne;
//    @Column
//    @OneToMany
//    private Player playerTwo;

    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShots = new char[10][10];
    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShips = new char[10][10];
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShots = new char[10][10];
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShips = new char[10][10];

    @Column(length = 100)
    @OneToMany
    private Player winnerPlayer;
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


//    public boolean initGame(Player playerOne, char[][] playerOneShips, Player playerTwo, char[][] playerTwoShips){
//
//    }

}
