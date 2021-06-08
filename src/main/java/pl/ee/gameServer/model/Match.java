package pl.ee.gameServer.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.*;
import pl.ee.gameServer.GameServer;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity @Data
@Table(name="matches")
public class Match {
    @Id
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    @Type(type="org.hibernate.type.UUIDBinaryType")
    private UUID uuid;
    @Column
    private boolean surrendered = false;
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
    private boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "shooting_player_uuid")
    @JsonIncludeProperties({"name","uuid"})
    private Player shootingPlayer;

}
