package pl.ee.gameServer.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity @Data
@Table(name="matches")
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
    private char[][] playerOneShots = new char[10][10];
    @Column(columnDefinition = "BLOB")
    private char[][] playerOneShips = new char[10][10];
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShots = new char[10][10];
    @Column(columnDefinition = "BLOB")
    private char[][] playerTwoShips = new char[10][10];


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
