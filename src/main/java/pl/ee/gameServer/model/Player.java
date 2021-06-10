package pl.ee.gameServer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.*;

@Entity @Data
@Table(name="players")
public class Player {
    @Id
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    @Type(type="org.hibernate.type.UUIDBinaryType")
    private UUID uuid;
    @Column(length = 100)
    private String name;
    @Column
    private int wins;
    @Column
    private int looses;
    @Column
    private int score;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "playerOne",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Match> playerOneGames = new ArrayList<>();

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "playerTwo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Match> playerTwoGames = new ArrayList<>();

    public void addPlayerOneGame(Match match) {
        playerOneGames.add(match);
        match.setPlayerOne(this);
    }
    public void addPlayerTwoGame(Match match) {
        playerTwoGames.add(match);
        match.setPlayerTwo(this);
    }


    public void removePlayerOneGame(Match match) {
        playerOneGames.remove(match);
        match.setPlayerOne(null);
    }
    public void removePlayerTwoGame(Match match) {
        playerTwoGames.remove(match);
        match.setPlayerTwo(null);
    }

    @JsonIgnore
    public List<Match> getPlayerGames(){
        List<Match> playerGames = new ArrayList<>();
        playerGames.addAll(playerOneGames);
        playerGames.addAll(playerTwoGames);
        return playerGames;
    }
}
