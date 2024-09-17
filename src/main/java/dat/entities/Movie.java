package dat.entities;

import dat.dtos.MovieDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Genre genre;
    private double duration;
    private double rating;
    private String overview;
    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "director_id")
    private Director director;

    @OneToMany(mappedBy = "actor")
    private Set<Actor> actors;

    public enum Genre{
        ACTION,
        ADVENTURE,
        COMEDY,
        DRAMA,
        FANTASY,
        HORROR,
        MYSTERY,
        ROMANCE,
        SCIENCE_FICTION,
        THRILLER,
        ANIMATION,
        DOCUMENTARY,
        BIOGRAPHY,
        CRIME,
        FAMILY,
        HISTORICAL,
        MUSICAL,
        WAR,
        WESTERN,
        SPORT,
        SUPERHERO
    }

    public Movie(MovieDTO movieDTO){
        this.id = movieDTO.getId();
        this.title = movieDTO.getTitle();
        this.genre = movieDTO.getGenre();
        this.duration = movieDTO.getDuration();
        this.rating = movieDTO.getRating();
        this.overview = movieDTO.getOverview();
        this.releaseDate = movieDTO.getReleaseDate();
        this.director = movieDTO.getDirector();
        this.actors = movieDTO.getActors();
    }
}
