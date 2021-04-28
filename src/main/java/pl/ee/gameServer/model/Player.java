package pl.ee.gameServer.model;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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

}
