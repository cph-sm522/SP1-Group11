package dat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.dtos.DirectorDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DirectorService {
    private static final String apiKey = System.getenv("api_key");

    public static DirectorDTO getDirectorInfo(Long movieId) {
        HttpResponse<String> response;
        ObjectMapper objectMapper = new ObjectMapper();
        String uri = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?language=en-US&api_key=" + apiKey;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(new URI(uri))
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse the response as a JSON object
                JsonNode jsonResponse = objectMapper.readTree(response.body());

                // Extract the "crew" array from the JSON response
                JsonNode crewArray = jsonResponse.get("crew");

                // Iterate over the crew array to find directors
                for (JsonNode crewMember : crewArray) {
                    String job = crewMember.get("job").asText();
                    if ("Director".equalsIgnoreCase(job)) {
                        // Extract director information
                        DirectorDTO director = new DirectorDTO();
                        director.setId(crewMember.get("id").asInt());
                        director.setName(crewMember.get("name").asText());
                        director.setJob(crewMember.get("job").asText());
                        return director; // Return the first director found
                    }
                }

                // If no director was found, return null or throw an exception
                System.out.println("No director found for movie ID: " + movieId);
                return null;
            } else {
                System.out.println("GET request failed. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
