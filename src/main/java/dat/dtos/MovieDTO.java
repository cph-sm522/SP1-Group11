package dat.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import dat.entities.Actor;
import dat.entities.Director;
import dat.entities.Movie;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Movie.Genre genre;
    private double duration;
    private double rating;
    private String overview;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    private DirectorDTO director;
    private Set<ActorDTO> actors;

    public MovieDTO(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.genre = movie.getGenre();
        this.duration = movie.getDuration();
        this.rating = movie.getRating();
        this.overview = movie.getOverview();
        this.releaseDate = movie.getReleaseDate();
        this.director = new DirectorDTO(movie.getDirector());
        this.actors = movie.getActors().stream()
                .map(ActorDTO::new)
                .collect(Collectors.toSet());
    }
}