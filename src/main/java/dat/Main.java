package dat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.dtos.ActorDTO;
import dat.dtos.DirectorDTO;
import dat.dtos.MovieDTO;
import dat.entities.Actor;
import dat.entities.Director;
import dat.entities.Movie;
import dat.services.ActorService;
import dat.services.DirectorService;
import dat.services.MovieService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("movieDB-SP1");
        final String apiKey = System.getenv("api_key");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key is not set. Please set the 'api_key' environment variable.");
        }

        alterTableMovies(emf);

        String responseBody = fetchMoviesFromAPI(apiKey);
        if (responseBody != null) {
            persistDataToDB(responseBody, emf);
        }
    }

    public static String fetchMoviesFromAPI(String apiKey) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&primary_release_date.gte=2019-01-01&sort_by=popularity.desc&with_origin_country=DK&api_key=" + apiKey))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {

            System.out.println(response.body());
            return response.body();
        } else {
            System.out.println("Request failed: " + response.statusCode());
            return null;
        }
    }

    public static void persistDataToDB(String responseBody, EntityManagerFactory emf) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode moviesNode = objectMapper.readTree(responseBody).get("results");

        try (var entityManager = emf.createEntityManager()) {
            var transaction = entityManager.getTransaction();
            transaction.begin();

            for (JsonNode movieNode : moviesNode) {
                try {
                    Long id = movieNode.get("id").asLong();
                    String title = movieNode.get("title").asText();
                    LocalDate releaseDate = movieNode.hasNonNull("release_date") ?
                            LocalDate.parse(movieNode.get("release_date").asText()) : null;

                    double rating = movieNode.get("vote_average").asDouble();
                    String overview = movieNode.get("overview").asText();

                    // Select only the first genre
                    Movie.Genre genre = Movie.Genre.fromId(movieNode.get("genre_ids").get(0).asInt());

                    DirectorDTO director = DirectorService.getDirectorInfo(id);
                    Set<ActorDTO> actors = ActorService.getActors(id);

                    MovieService.createMovie(id, title, releaseDate, genre, rating, overview, director, actors, emf);

                } catch (Exception e) {
                    System.err.println("Error processing movie: " + movieNode.get("title").asText() + " - " + e.getMessage());
                }
            }

            transaction.commit();
        } catch (Exception e) {
            System.err.println("Error persisting data to DB: " + e.getMessage());
        }
    }

    public static void alterTableMovies(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            var transaction = em.getTransaction();
            transaction.begin();

            String sql = "ALTER TABLE movies ALTER COLUMN overview TYPE VARCHAR(1000)";
            em.createNativeQuery(sql).executeUpdate();

            transaction.commit();
            System.out.println("Table 'movies' altered successfully.");
        } catch (Exception e) {
            System.err.println("Error altering table 'movies': " + e.getMessage());
        }
    }
}