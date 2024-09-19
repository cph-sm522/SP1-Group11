package dat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.dtos.ActorDTO;
import dat.dtos.DirectorDTO;
import dat.entities.Movie;
import dat.services.ActorService;
import dat.services.DirectorService;
import dat.services.MovieService;
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

        // Fetch and persist movies from all pages of the API
        fetchAndPersistMovies(apiKey, emf);
    }

    public static void fetchAndPersistMovies(String apiKey, EntityManagerFactory emf) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        int totalPages = 1;  // Initialize to 1, will be updated later
        int currentPage = 1;

        while (currentPage <= totalPages) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=" + currentPage + "&primary_release_date.gte=2019-01-01&sort_by=popularity.desc&with_origin_country=DK&api_key=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // Persist movie data to the database
                persistDataToDB(jsonResponse.get("results"), emf);

                // Update total pages based on API response
                totalPages = jsonResponse.get("total_pages").asInt();
                System.out.println("Current Page: " + currentPage + " / " + totalPages);

                currentPage++;
            } else {
                System.out.println("Request failed: " + response.statusCode());
                break; // Exit loop on request failure
            }
        }
    }

    private static int missingDataIdCounter = -1;

    public static ActorDTO createDefaultActor() {
        return new ActorDTO(missingDataIdCounter--, "no data");
    }

    public static DirectorDTO createDefaultDirector() {
        return new DirectorDTO(missingDataIdCounter--, "no data", "no job");
    }

    public static void persistDataToDB(JsonNode moviesNode, EntityManagerFactory emf) {
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

                    // Fetch director and actor data
                    DirectorDTO director = DirectorService.getDirectorInfo(id);
                    if (director == null) {
                        director = createDefaultDirector();
                    }

                    Set<ActorDTO> actors = ActorService.getActors(id);
                    if (actors == null || actors.isEmpty()) {
                        actors = new HashSet<>();
                        actors.add(createDefaultActor());
                    }

                    // Persist movie using MovieService
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
}
