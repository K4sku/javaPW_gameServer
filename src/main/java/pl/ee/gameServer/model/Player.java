package pl.ee.gameServer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "players_games",
            joinColumns = @JoinColumn(name = "player_uuid"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    public Set<Match> matches = new HashSet<>();

    public void addMatch(Match match) {
        this.matches.add(match);
        match.getPlayers().add(this);
    }

    public void removeMatch(Match match) {
        this.matches.remove(match);
        match.getPlayers().remove(this);
    }
}
