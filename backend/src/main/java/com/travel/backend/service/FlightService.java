package com.travel.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.backend.model.AirportResult;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightService.class);
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.airport-host}")
    private String airportHost;

    @Value("${rapidapi.airport-base-url}")
    private String airportBaseUrl;

    public List<AirportResult> searchAirports(String query) {
        HttpUrl url = HttpUrl.parse(airportBaseUrl + "/flights/searchAirport").newBuilder()
                .addQueryParameter("market", "US")
                .addQueryParameter("query", query)
                .addQueryParameter("locale", "en-US")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", airportHost)
                .addHeader("Content-Type", "application/json")
                .build();

        List<AirportResult> results = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);
            JsonNode places = root.path("places");
            if (places.isArray()) {
                for (JsonNode node : places) {
                    String skyId       = node.path("skyId").asText();
                    String entityId    = node.path("entityId").asText();
                    String name        = node.path("name").asText();
                    String countryName = node.path("countryName").asText();
                    results.add(new AirportResult(skyId, entityId, name, countryName));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to search airports: " + e.getMessage(), e);
        }
        return results;
    }
}
