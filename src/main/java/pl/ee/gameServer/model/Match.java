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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    @JsonIgnore
    @CreationTimestamp
    private LocalDateTime createDateTime;
    @JsonIgnore
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

    @JsonIgnore
    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShots = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
    @JsonIgnore
    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShips = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
    @JsonIgnore
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShots = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];
    @JsonIgnore
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShips = new char[GameServer.BOARD_SIZE][GameServer.BOARD_SIZE];

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_player_uuid")
    @JsonIncludeProperties({"name","uuid"})
    private Player winnerPlayer;
    @Column
    private boolean isActive;

    @Column(columnDefinition = "SMALLINT")
    private int playerOneFieldsRemainingCount = 99;
    @Column(columnDefinition = "SMALLINT")
    private int playerTwoFieldsRemainingCount= 99;

    @Column(columnDefinition = "BLOB")
    @JsonIgnore
    private HashMap<Character, Integer> playerTwoShipsRemainingMap;
    @Column(columnDefinition = "BLOB")
    @JsonIgnore
    private HashMap<Character, Integer> playerOneShipsRemainingMap;

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

    @Column
    private int lastShotHit = -1;

    @Column
    private boolean lastShotSunk = false;

    @JsonIgnore
    public void wipeShootsBoard() {
        char[] zeros = "0000000000".toCharArray();
        for (int i = 0; i < GameServer.BOARD_SIZE; i++){
            playerOneShots[i] = Arrays.copyOf(zeros,10);
            playerTwoShots[i] = Arrays.copyOf(zeros,10);
        }
    }

    @JsonIgnore
    public void calculateAndSetPlayerOneFieldsRemaining(){
        int count = 0;
        for (char [] row : playerOneShips) {
            for (char c : row){
                if (c != '0') count++;
            }
        }
        playerOneFieldsRemainingCount = count;
    }
    @JsonIgnore
    public void calculateAndSetPlayerTwoFieldsRemaining(){
        AtomicInteger count = new AtomicInteger();
        playerOneShipsRemainingMap.forEach((k, v) -> count.addAndGet(v));
        playerTwoFieldsRemainingCount = count.get();
    }
    @JsonIgnore
    public void calculateAndSetPlayerOneShipsRemainingMap(){
        HashMap<Character, Integer> playerShipsRemainingMap = new HashMap<>(5);
        for (char [] row : playerOneShips) {
            for (char c : row){
                if (c != '0') {
                    playerShipsRemainingMap.merge(c, 1, Integer::sum);
                }
            }
        }
        this.playerOneShipsRemainingMap = playerShipsRemainingMap;
    }
    @JsonIgnore
    public void calculateAndSetPlayerTwoShipsRemainingMap(){
        HashMap<Character, Integer> playerShipsRemainingMap = new HashMap<>(5);
        for (char [] row : playerTwoShips) {
            for (char c : row){
                if (c != '0') {
                    playerShipsRemainingMap.merge(c, 1, Integer::sum);
                }
            }
        }
        this.playerTwoShipsRemainingMap = playerShipsRemainingMap;
    }
    @JsonIgnore
    public void decrementPlayerOneFieldsRemaining(){
        int remainingFields = playerOneFieldsRemainingCount;
        playerOneFieldsRemainingCount = remainingFields - 1;

    }
    @JsonIgnore
    public void registerHitShipByPlayerOne(int shipType){
        int remainingFields = playerTwoFieldsRemainingCount;
        playerTwoFieldsRemainingCount = remainingFields - 1;
        playerTwoShipsRemainingMap.computeIfPresent(Character.forDigit(shipType, 10), (k, v) -> {
            v -= 1;
            if (v == 0) return null;
            return v;
        });
    }
    @JsonIgnore
    public void registerHitShipByPlayerTwo(int shipType){
        int remainingFields = playerOneFieldsRemainingCount;
        playerOneFieldsRemainingCount = remainingFields - 1;
        playerOneShipsRemainingMap.computeIfPresent(Character.forDigit(shipType, 10), (k, v) -> {
            v -= 1;
            if (v == 0) return null;
            return v;
        });
    }

    @JsonIgnore
    public void registerHit(Player shootingPlayer, int shipType){
        if(playerOne.equals(shootingPlayer)) {
            playerTwoFieldsRemainingCount -= 1;
            playerTwoShipsRemainingMap.computeIfPresent(Character.forDigit(shipType, 10), (k, v) -> {
                v -= 1;
                if (v == 0) return null;
                return v;
            });
        }
        if(playerTwo.equals(shootingPlayer)) {
            playerOneFieldsRemainingCount -= 1;
            playerOneShipsRemainingMap.computeIfPresent(Character.forDigit(shipType, 10), (k, v) -> {
                v -= 1;
                if (v == 0) return null;
                return v;
            });
        }
    }

    @JsonIgnore
    public String shortString(){
        return "match UUID: "+uuid.toString()+" playerOne [name: "+playerOne.getName()+" uuid: "+playerOne.getUuid()+" ] "+" playerTwo [name: "+playerTwo.getName()+" uuid: "+playerTwo.getUuid()+" ]";
    }

    @JsonIgnore
    public Player selectPlayer(Player player){
        if(playerOne.equals(player)) return playerOne;
        if(playerTwo.equals(player)) return playerTwo;
        return null;
    }

    @JsonIgnore
    public Player getOpponent(Player player){
        if(playerOne.equals(player)) return playerTwo;
        if(playerTwo.equals(player)) return playerOne;
        return null;
    }

    @JsonIgnore
    public char[][] selectPlayersShips(Player player){
        if(playerOne.equals(player)) return playerOneShips;
        if(playerTwo.equals(player)) return playerTwoShips;
        return null;
    }

    @JsonIgnore
    public char[][] selectPlayersShots(Player player){
        if(playerOne.equals(player)) return playerOneShots;
        if(playerTwo.equals(player)) return playerTwoShots;
        return null;
    }
    @JsonIgnore
    public void setPlayersShots(Player player, char[][] shots){
        if(playerOne.equals(player)) playerOneShots = shots;
        if(playerTwo.equals(player)) playerTwoShots = shots;
    }

    @JsonIgnore
    public HashMap<Character, Integer> selectPlayersShipsRemainingMap(Player player){
        if(playerOne.equals(player)) return playerOneShipsRemainingMap;
        if(playerTwo.equals(player)) return playerTwoShipsRemainingMap;
        return null;
    }

    @JsonIgnore
    public int selectPlayersFieldsRemainingCount(Player player){
        if(playerOne.equals(player)) return playerOneFieldsRemainingCount;
        if(playerTwo.equals(player)) return playerTwoFieldsRemainingCount;
        return 99;
    }

    @JsonIgnore
    public void setOpponentsTurn(Player player) {
        if (playerOne.equals(player)) setShootingPlayer(playerTwo);
        if (playerTwo.equals(player)) setShootingPlayer(playerOne);
    }
}
