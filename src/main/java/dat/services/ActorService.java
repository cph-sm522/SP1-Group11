package dat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.ActorDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

public class ActorService {
    private static final String apiKey = System.getenv("api_key");

    public static Set<ActorDTO> getActors(Long movieId) throws RuntimeException {
        HttpResponse<String> response;
        ObjectMapper objectMapper = new ObjectMapper();
        String uri = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?language=en-US&api_key=" + apiKey;

        Set<ActorDTO> actors = new HashSet<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(new URI(uri))
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode castArray = root.get("cast");

                if (castArray.isArray()) {
                    for (JsonNode castMember : castArray) {
                        if ("Acting".equals(castMember.get("known_for_department").asText())) {
                            int id = castMember.get("id").asInt();
                            String name = castMember.get("name").asText();
                            actors.add(new ActorDTO(id, name));
                        }
                    }
                }
            } else {
                System.out.println("GET request failed. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching actor data: " + e.getMessage(), e);
        }
        return actors;
    }
}
