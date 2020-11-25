package org.apache.ignite.internal.v2.builtins.config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.ignite.internal.v2.IgniteCLIException;

public class ConfigurationClient {

    private final String GET_URL = "/management/v1/configuration";
    private final String SET_URL = "/management/v1/configuration";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public ConfigurationClient(String baseUrl) {
        this.baseUrl = baseUrl;
        httpClient = HttpClient
            .newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    }

    public ConfigurationClient() {
        // TODO: url must be configurable
        this("http://localhost:8080");
    }

    public String get() {
        var request = HttpRequest
            .newBuilder()
            .GET()
            .header("Content-type", "application/json")
            .uri(URI.create(baseUrl + GET_URL))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(mapper.readValue(response.body(), JsonNode.class));
        }
        catch (IOException | InterruptedException e) {
            throw new IgniteCLIException("Connection issues while trying to send http request");
        }
    }

    public String set(String rawHoconData) {
        var config = ConfigFactory.parseString(rawHoconData);
        var jsonConfig = config.root().render(ConfigRenderOptions.concise());
        var request = HttpRequest
            .newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(jsonConfig))
            .header("Content-Type", "application/json")
            .uri(URI.create(baseUrl + SET_URL))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpURLConnection.HTTP_OK)
                return "";
            else
                return "Http error code: " + response.statusCode() + "\n" +
                    "Error message: " + response.body();
        }
        catch (IOException | InterruptedException e) {
            throw new IgniteCLIException("Connection issues while trying to send http request");
        }
    }
}
