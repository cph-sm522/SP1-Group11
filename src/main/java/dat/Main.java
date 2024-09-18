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
        String apiKey = System.getenv("api_key");

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

    public static void persistDataToDB(String responseBody, EntityManagerFactory emf) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode moviesNode = objectMapper.readTree(responseBody).get("results");

        for (JsonNode movieNode : moviesNode) {

            // henter filmdetaljer fra vores Json
            Long id = movieNode.get("id").asLong();
            String title = movieNode.get("title").asText();
            LocalDate releaseDate = LocalDate.parse(movieNode.get("release_date").asText());
            Movie.Genre genre = Movie.Genre.fromId(movieNode.get("genre_ids").get(0).asInt());
            double rating = movieNode.get("vote_average").asDouble();
            String overview = movieNode.get("overview").asText();

            DirectorDTO director = DirectorService.getDirectorInfo(id);
            Set<ActorDTO> actors = ActorService.getActors(id);

            // opret og persistere film
            MovieService.createMovie(title, releaseDate, genre, rating, overview, director, actors, emf);
        }
    }
}