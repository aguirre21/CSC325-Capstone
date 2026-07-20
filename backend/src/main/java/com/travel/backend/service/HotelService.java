package com.travel.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.backend.model.HotelLocationResult;
import com.travel.backend.model.HotelResult;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.hotel-host}")
    private String hotelHost;

    @Value("${rapidapi.hotel-base-url}")
    private String hotelBaseUrl;

    public List<HotelLocationResult> searchLocations(String query) {
        HttpUrl url = HttpUrl.parse(hotelBaseUrl + "/api/v1/hotels/searchLocation")
                .newBuilder()
                .addQueryParameter("query", query)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", rapidApiKey)
                .addHeader("x-rapidapi-host", hotelHost)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Hotel location search failed with status " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                return List.of();
            }

            JsonNode data = objectMapper.readTree(body.string()).path("data");
            List<HotelLocationResult> results = new ArrayList<>();

            if (data.isArray()) {
                for (JsonNode node : data) {
                    results.add(new HotelLocationResult(
                            node.path("geoId").asInt(),
                            stripHtml(node.path("title").asText()),
                            node.path("secondaryText").asText()
                    ));
                }
            }

            return results;
        } catch (IOException e) {
            throw new RuntimeException("Hotel location search failed", e);
        }
    }

    public List<HotelResult> searchHotels(int geoId, String checkIn, String checkOut) {
        try {
            HttpUrl url = HttpUrl.parse(hotelBaseUrl + "/api/v1/hotels/searchHotels")
                    .newBuilder()
                    .addQueryParameter("geoId", String.valueOf(geoId))
                    .addQueryParameter("checkIn", checkIn)
                    .addQueryParameter("checkOut", checkOut)
                    .addQueryParameter("pageNumber", "1")
                    .addQueryParameter("currencyCode", "USD")
                    .addQueryParameter("adults", "1")
                    .addQueryParameter("rooms", "1")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("x-rapidapi-key", rapidApiKey)
                    .addHeader("x-rapidapi-host", hotelHost)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Hotel search failed with status " + response.code());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    return List.of();
                }

                JsonNode hotels = objectMapper.readTree(body.string()).path("data").path("data");
                List<HotelResult> results = new ArrayList<>();

                if (hotels.isArray()) {
                    for (JsonNode node : hotels) {
                        HotelResult result = parseHotel(node);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }

                return results;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to search hotels: " + e.getMessage(), e);
        }
    }

    private HotelResult parseHotel(JsonNode node) {
        String priceForDisplay = node.path("priceForDisplay").asText();
        if (priceForDisplay == null || priceForDisplay.isBlank()) {
            return null;
        }

        String numericPrice = priceForDisplay.replaceAll("[^0-9.]", "");
        if (numericPrice.isBlank()) {
            return null;
        }

        double pricePerNight;
        try {
            pricePerNight = Double.parseDouble(numericPrice);
        } catch (NumberFormatException e) {
            return null;
        }

        HotelResult result = new HotelResult();
        result.setId(node.path("id").asText());
        result.setName(stripHtml(node.path("title").asText()));
        result.setLocation(node.path("secondaryInfo").asText(""));
        result.setRating(node.path("bubbleRating").path("rating").asDouble());
        result.setReviewCount(node.path("bubbleRating").path("count").asText(""));
        result.setPricePerNight(pricePerNight);
        result.setPriceFormatted(priceForDisplay);
        return result;
    }

    private String stripHtml(String text) {
        return text.replaceAll("<[^>]*>", "").trim();
    }
}
