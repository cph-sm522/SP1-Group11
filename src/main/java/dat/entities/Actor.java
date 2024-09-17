package dat.entities;

import dat.dtos.ActorDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@NamedQueries({
        @NamedQuery(name = "Actor.findByName", query = "SELECT a FROM Actor a WHERE a.name = :name"),
        @NamedQuery(name = "Actor.findAll", query = "SELECT a FROM Actor a")
})
public class Actor {
    @Id
    private int id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Movie movies;

    public Actor(ActorDTO actorDTO) {
        this.id = actorDTO.getId();
        this.name = actorDTO.getName();
    }
}
