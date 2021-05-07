package pl.ee.gameServer.model;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "player_games",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private Set<Match> matches = new HashSet<>();

    public void addMatch(Match match) {
        this.matches.add(match);
        match.getPlayers().add(this);
    }

    public void removeMatch(Match match) {
        this.matches.remove(match);
        match.getPlayers().remove(this);
    }
}
