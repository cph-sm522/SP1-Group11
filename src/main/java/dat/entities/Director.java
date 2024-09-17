package dat.entities;

import dat.dtos.DirectorDTO;
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

    public Director(DirectorDTO directorDTO) {
        this.id = directorDTO.getId();
        this.name = directorDTO.getName();
    }

}
