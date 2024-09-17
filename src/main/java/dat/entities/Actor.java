package dat.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Actor {
    @Id
    private int id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Movie movies;

}
