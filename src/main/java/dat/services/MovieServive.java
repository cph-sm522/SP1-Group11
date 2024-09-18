package dat.services;

import dat.daos.MovieDAO;
import dat.dtos.ActorDTO;
import dat.dtos.DirectorDTO;
import dat.dtos.MovieDTO;
import dat.entities.Actor;
import dat.entities.Director;
import dat.entities.Movie;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

public class MovieService {

    public static MovieDTO createMovie(String title, LocalDate releaseDate, Movie.Genre genre, double rating, String overview, Director director, Set<Actor> actors, EntityManagerFactory emf) throws IOException, InterruptedException {

        MovieDAO movieDAO = MovieDAO.getInstance(emf);
        DirectorDTO directorInfo = DirectorService.getDirector();
        ActorDTO actorInfo = ActorService.getActor();

        // Normally you would get this data from a form on a website or similar
        MovieDTO movieDTO = MovieDTO.builder()
                .title(title)
                .releaseDate(releaseDate)
                .genre(genre)
                .rating(rating)
                .overview(overview)
                .director(director)
                .actors(actors)
                .build();

        // Persist data to database
        String json = JsonService.convertObjectToJson(movieDTO);
        return movieDAO.createMovie(movieDTO);
    }

    public static MovieDTO updateMovie(EntityManagerFactory emf, MovieDTO activityDTO) {
        MovieDAO movieDAO = MovieDAO.getInstance(emf);
        // Potentially we could do some validation here before updating the activity
        return movieDAO.updateMovie(movieDTO);
    }
}
