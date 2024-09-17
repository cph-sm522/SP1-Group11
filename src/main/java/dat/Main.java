package dat;

import dat.config.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        // EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("persons");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&primary_release_date.gte=2019-01-01&sort_by=popularity.desc&with_origin_country=DK"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() == 200) {
            System.out.println(response.body());
        } else {
            System.out.println("request failed" + response.statusCode());
        }
    }
}