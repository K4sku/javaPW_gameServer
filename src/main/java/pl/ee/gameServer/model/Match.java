package pl.ee.gameServer.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.hibernate.annotations.*;
import pl.ee.gameServer.GameServer;
import pl.ee.gameServer.service.Coordinate;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity @Data
@Table(name="matches")
public class Match {
    @Id
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    @Type(type="org.hibernate.type.UUIDBinaryType")
    private UUID uuid;
    @Column(columnDefinition = "boolean default false")
    private boolean surrendered;
    @CreationTimestamp
    private LocalDateTime createDateTime;
    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"playerOneGames", "playerTwoGames"})
    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    private Player playerOne;
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"playerOneGames", "playerTwoGames"})
    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    private Player playerTwo;

    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShots = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShips = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShots = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShips = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_player_uuid")
    @JsonIncludeProperties({"name","uuid"})
    private Player winnerPlayer;
    @Column
    private boolean isActive;

    @Column(columnDefinition = "SMALLINT")
    @JsonIgnore
    private int playerOneFieldsRemaining;
    @JsonIgnore
    @Column(columnDefinition = "SMALLINT")
    private int playerTwoFieldsRemaining;

    @OneToOne
    @JoinColumn(name = "shooting_player_uuid")
    @JsonIncludeProperties({"name","uuid"})
    private Player shootingPlayer;

    @JsonIgnore
    public List<Player> getPlayersList(){
        List<Player> players = new ArrayList<>();
        players.add(playerOne);
        players.add(playerTwo);
        return players;
    }

    @Column
    @Embedded
    private Coordinate lastShot = new Coordinate();

    @JsonIgnore
    public void wipeShootsBoard() {
        char[] zeros = "0000000000".toCharArray();
        for (int i = 0; i < GameServer.BOARD_SIZE; i++){
            playerOneShots[i] = Arrays.copyOf(zeros,10);
            playerTwoShots[i] = Arrays.copyOf(zeros,10);
        }
    }

    public void calculateAndSetPlayerOneFieldsRemaining(){
        int count = 0;
        for (char [] row : playerOneShips) {
            for (char c : row){
                if (c != '0') count++;
            }
        }
        playerOneFieldsRemaining = count;
    }

    public void calculateAndSetPlayerTwoFieldsRemaining(){
        int count = 0;
        for (char [] row : playerTwoShips) {
            for (char c : row){
                if (c != '0') count++;
            }
        }
        playerTwoFieldsRemaining = count;
    }

    public void decrementPlayerOneFieldsRemaining(){
        int remainingFields = playerOneFieldsRemaining;
        playerOneFieldsRemaining = remainingFields - 1;
    }

    public void decrementPlayerTwoFieldsRemaining(){
        int remainingFields = playerTwoFieldsRemaining;
        playerTwoFieldsRemaining = remainingFields - 1;
    }

}
