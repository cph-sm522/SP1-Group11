package dat.daos;

import dat.dtos.ActorDTO;
import dat.dtos.MovieDTO;
import dat.entities.Actor;
import dat.entities.Director;
import dat.entities.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovieDAO {

    private static MovieDAO instance;
    private static EntityManagerFactory emf;

    private MovieDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static MovieDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new MovieDAO(emf);
        }
        return instance;
    }

    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = new Movie(movieDTO);
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();


            Director director = em.find(Director.class, movieDTO.getDirector().getId());
            if (director != null) {
                movie.setDirector(director);
            } else {
                em.persist(movie.getDirector());
            }

            Set<Actor> actors = new HashSet<>();
            for (ActorDTO actorDTO : movieDTO.getActors()) {
                Actor actor = em.find(Actor.class, actorDTO.getId());
                if (actor == null) {
                    actor = new Actor(actorDTO);
                    em.persist(actor);
                }
                actors.add(actor);
            }
            movie.setActors(actors);

            em.persist(movie);

            transaction.commit();
            return new MovieDTO(movie);

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creating movie", e);
        }
    }

    public MovieDTO updateMovie(MovieDTO movieDTO) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Movie movie = em.find(Movie.class, movieDTO.getId());
            if (movie == null) {
                throw new IllegalArgumentException("Movie with ID " + movieDTO.getId() + " not found.");
            }

            movie.setTitle(movieDTO.getTitle());
            movie.setDuration(movieDTO.getDuration());
            movie.setGenre(movieDTO.getGenre());
            movie.setOverview(movieDTO.getOverview());
            movie.setRating(movieDTO.getRating());
            movie.setReleaseDate(movieDTO.getReleaseDate());

            // check if it exists and update it if necessary
            Director director = em.find(Director.class, movieDTO.getDirector().getId());
            if (director != null) {
                movie.setDirector(director);
            } else {
                throw new IllegalArgumentException("Director with ID " + movieDTO.getDirector().getId() + " not found.");
            }

            // check if it exists and update it if necessary
            Set<Actor> actors = new HashSet<>();
            for (ActorDTO actorDTO : movieDTO.getActors()) {
                Actor actor = em.find(Actor.class, actorDTO.getId());
                if (actor == null) {
                    actor = new Actor(actorDTO);
                    em.persist(actor);
                }
                actors.add(actor);
            }
            movie.setActors(actors);

            em.merge(movie);
            em.getTransaction().commit();

            // return updated MovieDTO based on the updated entity
            return new MovieDTO(movie);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public static MovieDTO getMovieById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return new MovieDTO(em.find(Movie.class, id));
        }
    }

    public static MovieDTO getMovieByTitle(String title) {
        try (EntityManager em = emf.createEntityManager()) {
            Movie movie = em.createQuery("SELECT m FROM Movie m WHERE m.title = :title", Movie.class)
                    .setParameter("title", title)
                    .getSingleResult();
            return new MovieDTO(movie);
        } catch (NoResultException e) {
            // Handle case where no movie is found
            return null;
        }
    }

    public static List<MovieDTO> getAllMovies() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT new dat.dtos.MovieDTO(m) FROM Movie m", MovieDTO.class).getResultList();
        }
    }

    public static List<MovieDTO> getAllByGenreMovies(Movie.Genre genre) { //chose based on genre
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT new dat.dtos.MovieDTO(m) FROM Movie m WHERE m.genre = :genre", MovieDTO.class)
                    .setParameter("genre", genre)
                    .getResultList();
        } catch (NoResultException e) {

            return List.of();
        }
    }

    public void deleteMovie(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Movie movie = em.find(Movie.class, id);
            if (movie != null) {
                em.remove(movie);
            }
            em.getTransaction().commit();
        }
    }

    public static void printAllGenres() {
        for (Movie.Genre genre : Movie.Genre.values()) {
            System.out.println(genre.getName());
        }
    }

    public List<Movie> getMoviesByGenre(String name) {
        int genreId = 0;
        String genreName = name.toLowerCase();

        switch (genreName) {
            case "action":
                genreId = 28;
                break;
            case "adventure":
                genreId = 12;
                break;
            case "animation":
                genreId = 16;
                break;
            case "comedy":
                genreId = 35;
                break;
            case "crime":
                genreId = 80;
                break;
            case "documentary":
                genreId = 99;
                break;
            case "drama":
                genreId = 18;
                break;
            case "family":
                genreId = 10751;
                break;
            case "fantasy":
                genreId = 14;
                break;
            case "history":
                genreId = 36;
                break;
            case "horror":
                genreId = 27;
                break;
            case "music":
                genreId = 10402;
                break;
            case "mystery":
                genreId = 9648;
                break;
            case "romance":
                genreId = 10749;
                break;
            case "science fiction":
                genreId = 878;
                break;
            case "tv movie":
                genreId = 10770;
                break;
            case "thriller":
                genreId = 53;
                break;
            case "war":
                genreId = 10752;
                break;
            case "western":
                genreId = 37;
                break;
            default:
                throw new IllegalArgumentException("Unknown genre: " + name);
        }

        return getMoviesByGenreId(genreId);
    }

    private List<Movie> getMoviesByGenreId(int genreId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT m FROM Movie m WHERE m.genre = :genreId", Movie.class)
                    .setParameter("genreId", genreId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<MovieDTO> getTop10Movies() {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT new dat.dtos.MovieDTO(m) FROM Movie m ORDER BY m.rating DESC", MovieDTO.class)
                .setMaxResults(10)
                .getResultList();
    }


    public double getAverageRating() {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT AVG(m.rating) FROM Movie m", Double.class).getSingleResult();

    }
}