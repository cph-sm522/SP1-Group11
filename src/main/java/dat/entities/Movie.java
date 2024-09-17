package dat.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;
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


    private Director director;
    private Actor actor;

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
}
