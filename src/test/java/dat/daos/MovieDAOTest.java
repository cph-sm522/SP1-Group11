package dat.daos;

import dat.config.HibernateConfig;

import dat.dtos.MovieDTO;
import dat.entities.Movie;
import dat.services.JsonService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class MovieDAOTest {
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final MovieDAO movieDAO = MovieDAO.getInstance(emf);
    private static MovieDTO m1;
    private static MovieDTO m2;

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Clear existing data
            em.createQuery("DELETE FROM Movie").executeUpdate();
            em.createQuery("DELETE FROM Actor").executeUpdate();
            em.createQuery("DELETE FROM Director").executeUpdate();

            em.getTransaction().commit();

            String m1Json = "{\"id\":1,\"title\":\"Inception\",\"genre\":\"SCIENCE_FICTION\",\"duration\":148.0,\"rating\":8.8,\"overview\":\"A thief who steals corporate secrets through the use of dream-sharing technology is tasked with planting an idea in a CEO's mind.\",\"releaseDate\":\"2010-07-16\",\"director\":{\"id\":1,\"name\":\"Christopher Nolan\"},\"actors\":[{\"id\":1,\"name\":\"Leonardo DiCaprio\"},{\"id\":2,\"name\":\"Joseph Gordon-Levitt\"}]}";
            m1 = JsonService.convertJsonToObject(m1Json, MovieDTO.class);
            movieDAO.createMovie(m1);

            String m2Json = "{\"id\":2,\"title\":\"The Matrix\",\"genre\":\"ACTION\",\"duration\":136.0,\"rating\":8.7,\"overview\":\"A hacker learns about the true nature of reality and his role in the war against its controllers.\",\"releaseDate\":\"1999-03-31\",\"director\":{\"id\":2,\"name\":\"Wachowskis\"},\"actors\":[{\"id\":3,\"name\":\"Keanu Reeves\"},{\"id\":4,\"name\":\"Laurence Fishburne\"}]}";
            m2 = JsonService.convertJsonToObject(m2Json, MovieDTO.class);
            movieDAO.createMovie(m2);
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getMovieById() {
        MovieDTO foundMovie = movieDAO.getMovieById(m1.getId());
        assertNotNull(foundMovie);
        assertEquals(m1.getId(), foundMovie.getId());
    }

    @Test
    void getMovieByTitle() {
        MovieDTO foundMovie = movieDAO.getMovieByTitle(m1.getTitle());
        assertNotNull(foundMovie);
        assertEquals(m1.getTitle(), foundMovie.getTitle());
    }

    @Test
    void getAllMovies() {
        List<MovieDTO> movies = movieDAO.getAllMovies();
        assertEquals(2, movies.size());
    }

    @Test
    void getAllByGenreMovies() {
        //action
        List<MovieDTO> actionMovies = movieDAO.getAllByGenreMovies(Movie.Genre.ACTION);
        assertEquals(1, actionMovies.size());
        assertEquals(m2.getTitle(), actionMovies.get(0).getTitle());

        //science_fiction
        List<MovieDTO> sciFiMovies = movieDAO.getAllByGenreMovies(Movie.Genre.SCIENCE_FICTION);
        assertEquals(1, sciFiMovies.size());
        assertEquals(m1.getTitle(), sciFiMovies.get(0).getTitle());

        //genre with no movies
        List<MovieDTO> dramaMovies = movieDAO.getAllByGenreMovies(Movie.Genre.DRAMA);
        assertEquals(0, dramaMovies.size());
    }

    @Test
    void deleteMovie() {
        MovieDTO foundMovie = movieDAO.getMovieById(m1.getId());
        assertNotNull(foundMovie);

        movieDAO.deleteMovie(m1.getId());

    }

    @Test
    void printAllGenres() {
        // Capture console output
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        movieDAO.printAllGenres();

        // Check output content
        String output = outContent.toString().trim();
        assertTrue(output.contains("Action"));
        assertTrue(output.contains("Adventure"));
        assertTrue(output.contains("Animation"));


        System.setOut(System.out);
    }

}
