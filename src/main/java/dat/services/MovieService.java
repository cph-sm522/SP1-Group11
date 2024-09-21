package dat.services;

import dat.daos.MovieDAO;
import dat.dtos.ActorDTO;
import dat.dtos.DirectorDTO;
import dat.dtos.MovieDTO;
import dat.entities.Movie;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

public class MovieService {

    public static MovieDTO createMovie(Long id, String title, LocalDate releaseDate, Movie.Genre genre, double rating, String overview, DirectorDTO director, Set<ActorDTO> actors, EntityManagerFactory emf) throws IOException, InterruptedException {

        MovieDAO movieDAO = MovieDAO.getInstance(emf);

        // Create MovieDTO object
        MovieDTO movieDTO = MovieDTO.builder()
                .id(id)
                .title(title)
                .releaseDate(releaseDate)
                .genre(genre)
                .rating(rating)
                .overview(overview)
                .director(director)
                .actors(actors)
                .build();

        // Convert movieDTO to JSON string
        String json = JsonService.convertObjectToJson(movieDTO);
        System.out.println("Movie Data as JSON: " + json); // For logging or debugging

        return movieDAO.createMovie(movieDTO);
    }
}
