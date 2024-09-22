package dat.daos;

import dat.config.HibernateConfig;
import dat.dtos.ActorDTO;
import dat.dtos.DirectorDTO;
import dat.dtos.MovieDTO;
import dat.entities.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestMovieDAO {

    static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    static MovieDAO movieDAO = MovieDAO.getInstance(emf);
    MovieDTO movie1, movie2;
    DirectorDTO director;
    Set<ActorDTO> actors;

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Movie").executeUpdate(); // Clear the movies
            em.createQuery("DELETE FROM Actor").executeUpdate(); // Clear actors
            em.createQuery("DELETE FROM Director").executeUpdate(); // Clear directors
            em.getTransaction().commit();

            director = DirectorDTO.builder()
                    .id(1)
                    .name("Christopher Nolan")
                    .job("Director")
                    .build();

            actors = new HashSet<>();
            actors.add(ActorDTO.builder().id(1).name("Christian Bale").build());
            actors.add(ActorDTO.builder().id(2).name("Michael Caine").build());

            movie1 = MovieDTO.builder()
                    .id(1L)
                    .title("Inception")
                    .genre(Movie.Genre.SCIENCE_FICTION)
                    .duration(148)
                    .rating(8.8)
                    .overview("A thief who steals corporate secrets through the use of dream-sharing technology.")
                    .releaseDate(LocalDate.of(2010, 7, 16))
                    .director(director)
                    .actors(actors)
                    .build();

            movie2 = MovieDTO.builder()
                    .id(2L)
                    .title("The Dark Knight")
                    .genre(Movie.Genre.ACTION)
                    .duration(152)
                    .rating(9.0)
                    .overview("When the menace known as The Joker wreaks havoc on Gotham City.")
                    .releaseDate(LocalDate.of(2008, 7, 18))
                    .director(director)
                    .actors(actors)
                    .build();

            movieDAO.createMovie(movie1);
            movieDAO.createMovie(movie2);
        }
    }

    @AfterEach
    void tearDown() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Movie").executeUpdate();
            em.createQuery("DELETE FROM Actor").executeUpdate();
            em.createQuery("DELETE FROM Director").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void testCreateMovie() {
        MovieDTO movie = movieDAO.getMovieById(1L);
        assertNotNull(movie, "Movie should be created successfully.");
        assertEquals("Inception", movie.getTitle(), "Movie title should match.");
    }



    @Test
    void testUpdateMovie() {
        MovieDTO movie = movieDAO.getMovieById(1L);
        assertNotNull(movie, "Movie should be fetched successfully.");

        movie.setTitle("Inception - Updated");
        Set<ActorDTO> newActors = new HashSet<>();
        newActors.add(ActorDTO.builder().id(3).name("Tom Hardy").build()); // Adding new actor
        movie.setActors(newActors);

        movieDAO.updateMovie(movie);

        MovieDTO updatedMovie = movieDAO.getMovieById(1L);
        assertEquals("Inception - Updated", updatedMovie.getTitle());
        assertEquals(1, updatedMovie.getActors().size());
        System.out.println("TEST WORKED");
    }
}
