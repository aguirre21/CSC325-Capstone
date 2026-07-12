package com.travel.frontend.controller;

import com.travel.frontend.model.HotelInfo;
import com.travel.frontend.model.TripSession;
import com.travel.frontend.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelController {

    @FXML private TextField locationQueryField;
    @FXML private ListView<String> locationList;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private TableView<HotelInfo> resultsTable;
    @FXML private TableColumn<HotelInfo, String> colName;
    @FXML private TableColumn<HotelInfo, String> colLocation;
    @FXML private TableColumn<HotelInfo, String> colRating;
    @FXML private TableColumn<HotelInfo, String> colPrice;
    @FXML private Label statusLabel;
    @FXML private Label selectedHotelLabel;

    private final ObservableList<HotelInfo> hotels = FXCollections.observableArrayList();
    private final List<JsonNode> locations = new ArrayList<>();

    private JsonNode selectedLocation;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));

        resultsTable.setItems(hotels);
    }

    @FXML
    private void searchLocations() {
        String query = locationQueryField.getText();
        if (query.isBlank()) return;
        setStatus("Searching locations...", false);

        new Thread(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String json = ApiClient.getRaw("/api/hotels/locations?query=" + encodedQuery);
                JsonNode arr = ApiClient.getMapper().readTree(json);
                List<String> names = new ArrayList<>();
                locations.clear();
                if (arr.isArray()) {
                    for (JsonNode n : arr) {
                        locations.add(n);
                        names.add(n.path("name").asText() + " — " + n.path("subtitle").asText());
                    }
                }
                Platform.runLater(() -> {
                    locationList.setItems(FXCollections.observableArrayList(names));
                    setStatus(names.isEmpty() ? "No locations found." : names.size() + " location(s) found — click one to select.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Location search failed: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void selectLocation() {
        int idx = locationList.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < locations.size()) {
            selectedLocation = locations.get(idx);
            locationQueryField.setText(locationList.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void searchHotels() {
        if (selectedLocation == null) {
            setStatus("Please select a location.", true);
            return;
        }
        if (checkInPicker.getValue() == null || checkOutPicker.getValue() == null) {
            setStatus("Please select check-in and check-out dates.", true);
            return;
        }
        if (!checkOutPicker.getValue().isAfter(checkInPicker.getValue())) {
            setStatus("Check-out date must be after check-in.", true);
            return;
        }

        int geoId = selectedLocation.path("geoId").asInt();
        LocalDate checkIn = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        setStatus("Searching hotels...", false);
        hotels.clear();

        new Thread(() -> {
            try {
                String url = "/api/hotels/search?geoId=" + geoId
                        + "&checkIn=" + checkIn
                        + "&checkOut=" + checkOut;

                String json = ApiClient.getRaw(url);
                JsonNode arr = ApiClient.getMapper().readTree(json);
                List<HotelInfo> found = new ArrayList<>();
                if (arr.isArray()) {
                    for (JsonNode n : arr) {
                        found.add(new HotelInfo(
                                n.path("id").asText(),
                                n.path("name").asText(),
                                n.path("location").asText(),
                                n.path("rating").asDouble(),
                                n.path("reviewCount").asText(),
                                n.path("pricePerNight").asDouble(),
                                n.path("priceFormatted").asText(),
                                checkIn, checkOut
                        ));
                    }
                }
                Platform.runLater(() -> {
                    hotels.setAll(found);
                    setStatus(found.size() + " hotel(s) found.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Search failed: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void selectHotel() {
        HotelInfo h = resultsTable.getSelectionModel().getSelectedItem();
        if (h == null) {
            setStatus("Please select a hotel from the table.", true);
            return;
        }
        TripSession s = TripSession.get();
        s.addHotel(h);

        selectedHotelLabel.setText(s.getHotels().size() + " hotel(s) added to trip — see Budget tab.");
        setStatus("Hotel added to trip!", false);
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
    }
}