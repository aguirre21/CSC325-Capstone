package com.travel.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.backend.model.AirportResult;
import com.travel.backend.model.FlightResult;
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

    @Value("${rapidapi.flight-host}")
    private String flightHost;

    @Value("${rapidapi.flight-base-url}")
    private String flightBaseUrl;

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

    public List<FlightResult> searchFlights(String originSkyId, String destinationSkyId,
                                             String originEntityId, String destinationEntityId,
                                             String date, int adults, String cabinClass) {
        HttpUrl url = HttpUrl.parse(flightBaseUrl + "/flights/searchFlights").newBuilder()
                .addQueryParameter("originSkyId", originSkyId)
                .addQueryParameter("destinationSkyId", destinationSkyId)
                .addQueryParameter("originEntityId", originEntityId)
                .addQueryParameter("destinationEntityId", destinationEntityId)
                .addQueryParameter("date", date)
                .addQueryParameter("cabinClass", cabinClass)
                .addQueryParameter("adults", String.valueOf(adults))
                .addQueryParameter("childrens", "0")
                .addQueryParameter("infants", "0")
                .addQueryParameter("currency", "USD")
                .addQueryParameter("market", "US")
                .addQueryParameter("countryCode", "US")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", flightHost)
                .addHeader("Content-Type", "application/json")
                .build();

        List<FlightResult> results = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);

            JsonNode itineraries = root.path("itineraries");
            if (itineraries.isArray()) {
                for (JsonNode itin : itineraries) {
                    FlightResult flight = parseItinerary(itin);
                    if (flight != null) results.add(flight);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to search flights: " + e.getMessage(), e);
        }
        return results;
    }

    private FlightResult parseItinerary(JsonNode itin) {
        try {
            FlightResult f = new FlightResult();
            f.setItineraryId(itin.path("id").asText());
            f.setPriceRaw(itin.path("price").path("amount").asDouble());
            f.setPriceFormatted(itin.path("price").path("formatted").asText());

            JsonNode legs = itin.path("legs");
            if (legs.isArray() && legs.size() > 0) {
                JsonNode leg = legs.get(0);
                f.setOriginCode(leg.path("origin").asText());
                f.setDestinationCode(leg.path("destination").asText());
                f.setDepartureTime(leg.path("departure").asText());
                f.setArrivalTime(leg.path("arrival").asText());
                f.setDurationMinutes(leg.path("durationMinutes").asInt());
                f.setStopCount(leg.path("stopCount").asInt());

                JsonNode carriers = leg.path("carriers");
                if (carriers.isArray() && carriers.size() > 0) {
                    f.setAirline(carriers.get(0).path("name").asText());
                }
            }
            return f;
        } catch (Exception e) {
            return null;
        }
    }
}
