package dat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.dtos.ActorDTO;
import dat.dtos.DirectorDTO;
import dat.dtos.MovieDTO;
import dat.entities.Movie;
import dat.services.ActorService;
import dat.services.DirectorService;
import dat.services.JsonService;
import dat.services.MovieService;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("movieDB-SP1");
        final String apiKey = System.getenv("api_key");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key is not set. Please set the 'api_key' environment variable.");
        }

        String responseBody = fetchMoviesFromAPI(apiKey);
        if (responseBody != null) {
            persistDataToDB(responseBody, emf);
        }
    }

    public static String fetchMoviesFromAPI(String apiKey) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US" +
                "&page=1&primary_release_date.gte=2019-01-01&with_origin_country=DK&sort_by=popularity.desc&api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
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
                    MovieDTO movieDTO = JsonService.convertJsonToObject(movieNode.toString(), MovieDTO.class);

                    if (movieDTO == null) {
                        throw new IllegalArgumentException("Invalid movie data in the JSON response");
                    }

                    if (movieNode.has("genre_ids")) {
                        List<Integer> genreIds = objectMapper.convertValue(movieNode.get("genre_ids"), List.class);
                        movieDTO.setGenreIds(genreIds);
                    }

                    DirectorDTO director = DirectorService.getDirectorInfo(movieDTO.getId());
                    Set<ActorDTO> actors = ActorService.getActors(movieDTO.getId());

                    movieDTO.setDirector(director);
                    movieDTO.setActors(actors);

                    MovieService.createMovie(movieDTO.getId(), movieDTO.getTitle(), movieDTO.getReleaseDate(),
                            movieDTO.getGenre(), movieDTO.getRating(), movieDTO.getOverview(),
                            director, actors, emf);

                    System.out.println("Successfully persisted movie: " + movieDTO.getTitle());

                } catch (Exception e) {
                    System.out.println("Error processing movie: " + movieNode.get("title").asText() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            System.out.println("Error persisting data to DB: " + e.getMessage());
        }
    }

}