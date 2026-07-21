package com.travel.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.backend.model.HotelLocationResult;
import com.travel.backend.model.HotelResult;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class HotelService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.hotel-host}")
    private String hotelHost;

    @Value("${rapidapi.hotel-base-url}")
    private String hotelBaseUrl;

    public List<HotelLocationResult> searchLocations(String query) {
        HttpUrl url = HttpUrl.parse(hotelBaseUrl + "/api/v1/hotels/searchLocation").newBuilder()
                .addQueryParameter("query", query)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", hotelHost)
                .addHeader("Content-Type", "application/json")
                .build();

        List<HotelLocationResult> results = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);
            checkForApiError(root);
            JsonNode data = root.path("data");
            if (data.isArray()) {
                for (JsonNode node : data) {
                    int geoId = node.path("geoId").asInt();
                    String title = stripHtml(node.path("title").asText());
                    String subtitle = node.path("secondaryText").asText();
                    results.add(new HotelLocationResult(geoId, title, subtitle));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to search hotel locations: " + e.getMessage(), e);
        }
        return results;
    }

    public List<HotelResult> searchHotels(int geoId, String checkIn, String checkOut) {
        HttpUrl url = HttpUrl.parse(hotelBaseUrl + "/api/v1/hotels/searchHotels").newBuilder()
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
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", hotelHost)
                .addHeader("Content-Type", "application/json")
                .build();

        List<HotelResult> results = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);
            checkForApiError(root);
            JsonNode hotels = root.path("data").path("data");
            if (hotels.isArray()) {
                for (JsonNode node : hotels) {
                    HotelResult h = parseHotel(node);
                    if (h != null) results.add(h);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to search hotels: " + e.getMessage(), e);
        }
        return results;
    }

    private HotelResult parseHotel(JsonNode node) {
        String priceText = node.path("priceForDisplay").asText(null);
        if (priceText == null || priceText.isBlank()) return null;
        double price;
        try {
            price = Double.parseDouble(priceText.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }

        HotelResult h = new HotelResult();
        h.setId(node.path("id").asText());
        h.setName(stripHtml(node.path("title").asText()));
        h.setLocation(node.path("secondaryInfo").asText(""));
        h.setRating(node.path("bubbleRating").path("rating").asDouble());
        h.setReviewCount(node.path("bubbleRating").path("count").asText(""));
        h.setPricePerNight(price);
        h.setPriceFormatted(priceText);
        return h;
    }

    private void checkForApiError(JsonNode root) {
        if (root.has("data")) return;
        JsonNode message = root.path("message");
        if (!message.isMissingNode() && !message.isNull()) {
            throw new RuntimeException("Hotel API error: " + message.asText());
        }
    }

    private String stripHtml(String text) {
        return text == null ? "" : text.replaceAll("<[^>]*>", "").trim();
    }
}
