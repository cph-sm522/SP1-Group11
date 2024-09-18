package dat.entities;

import dat.dtos.ActorDTO;
import dat.dtos.MovieDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Movie {
    @Id
    private Long id;
    private String title;
    @Enumerated(EnumType.STRING)
    private Genre genre;

    private double duration;
    private double rating;
    private String overview;
    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "director_id")
    private Director director;

    @ManyToMany
    @JoinTable(name = "movie_actors",
            joinColumns = @JoinColumn(name = "actor_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private Set<Actor> actors;

    public enum Genre {
        ACTION(28, "Action"),
        ADVENTURE(12, "Adventure"),
        ANIMATION(16, "Animation"),
        COMEDY(35, "Comedy"),
        CRIME(80, "Crime"),
        DOCUMENTARY(99, "Documentary"),
        DRAMA(18, "Drama"),
        FAMILY(10751, "Family"),
        FANTASY(14, "Fantasy"),
        HISTORY(36, "History"),
        HORROR(27, "Horror"),
        MUSIC(10402, "Music"),
        MYSTERY(9648, "Mystery"),
        ROMANCE(10749, "Romance"),
        SCIENCE_FICTION(878, "Science Fiction"),
        TV_MOVIE(10770, "TV Movie"),
        THRILLER(53, "Thriller"),
        WAR(10752, "War"),
        WESTERN(37, "Western");

        private final int genre_ids;
        private final String name;

        Genre(int genreId, String name) {
            this.genre_ids = genreId;
            this.name = name;
        }

        public int getGenreId() {
            return genre_ids;
        }

        public String getName() {
            return name;
        }

        public static Genre fromId(int id) {
            for (Genre genre : values()) {
                if (genre.getGenreId() == id) {
                    return genre;
                }
            }
            throw new IllegalArgumentException("No enum constant for genre id: " + id);
        }
    }

    public Movie(MovieDTO movieDTO) {
        this.id = movieDTO.getId();
        this.title = movieDTO.getTitle();
        this.genre = movieDTO.getGenre();
        this.duration = movieDTO.getDuration();
        this.rating = movieDTO.getRating();
        this.overview = movieDTO.getOverview();
        this.releaseDate = movieDTO.getReleaseDate();
        if (movieDTO.getDirector() != null) {
            this.director = new Director(movieDTO.getDirector());
        }
        if (movieDTO.getActors() != null) {
            this.actors = movieDTO.getActors().stream()
                    .map(Actor::new)
                    .collect(Collectors.toSet());
        } else {
            this.actors = new HashSet<>();
        }
    }

}
