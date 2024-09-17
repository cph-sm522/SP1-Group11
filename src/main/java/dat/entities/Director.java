package dat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Director{

    @Id
    private int id;
    private String name;

    @OneToMany(mappedBy = "director")
    private Set<Movie> movies;


}
