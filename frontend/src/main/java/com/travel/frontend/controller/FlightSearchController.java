package com.travel.frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.frontend.model.FlightInfo;
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
import java.util.ArrayList;
import java.util.List;

public class FlightSearchController {
    @FXML private TextField originQueryField;
    @FXML private ListView<String> originList;
    @FXML private TextField destQueryField;
    @FXML private ListView<String> destList;
    @FXML private DatePicker flightDatePicker;
    @FXML private ComboBox<String> cabinClassCombo;
    @FXML private TableView<FlightInfo> resultsTable;
    @FXML private TableColumn<FlightInfo, String> colAirline;
    @FXML private TableColumn<FlightInfo, String> colRoute;
    @FXML private TableColumn<FlightInfo, String> colDeparture;
    @FXML private TableColumn<FlightInfo, String> colArrival;
    @FXML private TableColumn<FlightInfo, String> colDuration;
    @FXML private TableColumn<FlightInfo, String> colStops;
    @FXML private TableColumn<FlightInfo, String> colPrice;
    @FXML private Label statusLabel;
    @FXML private Label selectedFlightLabel;

    //List used to store flight results and display results on table
    private final ObservableList<FlightInfo> flights = FXCollections.observableArrayList();
    //Holds results from API
    private final List<JsonNode> originAirports = new ArrayList<>();
    private final List<JsonNode> destAirports = new ArrayList<>();

    //Store user search query
    private JsonNode selectedOrigin;
    private JsonNode selectedDest;


    /**
     * Sets default values of FXML elements when FXML is loaded. Elements include drop down cabin class menu, Table
     * column heading.
     */
    @FXML
    public void initialize() {
        cabinClassCombo.setItems(FXCollections.observableArrayList("economy", "premium_economy", "business", "first"));
        cabinClassCombo.setValue("economy");

        colAirline.setCellValueFactory(new PropertyValueFactory<>("airline"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departure"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrival"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colStops.setCellValueFactory(new PropertyValueFactory<>("stops"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        resultsTable.setItems(flights);
    }

    /**
     * Following methods gets user input for Origin/ destination Airport and uses SearchAirport method to Query API
     */
    @FXML
    private void searchOriginAirports() {
        searchAirports(originQueryField.getText(), originList, originAirports, true);
    }

    @FXML
    private void searchDestAirports() {
        searchAirports(destQueryField.getText(), destList, destAirports, false);
    }

    /**
     * Checks to see if input is blank prior to starting query to API using that input. Takes that response
     * and stores it. Displays the stored responses in table.
     *
     * @param query user input
     * @param list saved response from API to be displayed
     * @param store holds response from API
     * @param isOrigin
     */
    private void searchAirports(String query, ListView<String> list, List<JsonNode> store, boolean isOrigin) {
        if (query.isBlank()) return;
        setStatus("Searching airports...", false);
        new Thread(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String json = ApiClient.getRaw("/api/flights/airports?query=" + encodedQuery);
                JsonNode arr = ApiClient.getMapper().readTree(json);
                List<String> names = new ArrayList<>();
                store.clear();
                if (arr.isArray()) {
                    for (JsonNode n : arr) {
                        store.add(n);
                        names.add(n.path("name").asText() + " — " + n.path("subtitle").asText());
                    }
                }
                Platform.runLater(() -> {
                    list.setItems(FXCollections.observableArrayList(names));
                    setStatus(names.isEmpty() ? "No airports found." : names.size() + " airport(s) found — click one to select.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Airport search failed: " + e.getMessage(), true));
            }
        }).start();
    }


    /**
     * Gets user selection from list view of API response and replaces the selected airport in the origin text field.
     */
    @FXML
    private void selectOrigin() {
        int idx = originList.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < originAirports.size()) {
            selectedOrigin = originAirports.get(idx);
            originQueryField.setText(originList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Gets user selection from list view of API response and replaces the selected airport in the destination text field
     */
    @FXML
    private void selectDest() {
        int idx = destList.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < destAirports.size()) {
            selectedDest = destAirports.get(idx);
            destQueryField.setText(destList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Makes sure that user has selected origin and destination or else it prompts user. Once both fields are verified.
     * Starts a background thread to query API and displays response in tabel.
     */
    @FXML
    private void searchFlights() {
        if (selectedOrigin == null || selectedDest == null) {
            setStatus("Please select origin and destination airports.", true);
            return;
        }
        if (flightDatePicker.getValue() == null) {
            setStatus("Please select a flight date.", true);
            return;
        }

        String originSkyId = selectedOrigin.path("skyId").asText();
        String destSkyId = selectedDest.path("skyId").asText();
        String originEntityId = selectedOrigin.path("entityId").asText();
        String destEntityId = selectedDest.path("entityId").asText();
        String date = flightDatePicker.getValue().toString();
        int adults = TripSession.get().getTravelerCount();
        String cabin = cabinClassCombo.getValue();

        setStatus("Searching flights...", false);
        flights.clear();

        new Thread(() -> {
            try {
                String url = "/api/flights/search?originSkyId=" + originSkyId
                        + "&destinationSkyId=" + destSkyId
                        + "&originEntityId=" + originEntityId
                        + "&destinationEntityId=" + destEntityId
                        + "&date=" + date
                        + "&adults=" + adults
                        + "&cabinClass=" + cabin;

                String json = ApiClient.getRaw(url);
                JsonNode arr = ApiClient.getMapper().readTree(json);
                List<FlightInfo> found = new ArrayList<>();
                if (arr.isArray()) {
                    for (JsonNode n : arr) {
                        found.add(new FlightInfo(
                                n.path("itineraryId").asText(),
                                n.path("airline").asText(),
                                n.path("flightNumber").asText(),
                                n.path("originCode").asText(),
                                n.path("destinationCode").asText(),
                                n.path("originCity").asText(""),
                                n.path("destinationCity").asText(""),
                                n.path("departureTime").asText(),
                                n.path("arrivalTime").asText(),
                                n.path("durationMinutes").asInt(),
                                n.path("stopCount").asInt(),
                                n.path("priceRaw").asDouble(),
                                n.path("priceFormatted").asText()
                        ));
                    }
                }
                Platform.runLater(() -> {
                    flights.setAll(found);
                    setStatus(found.size() + " flight(s) found.", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Search failed: " + e.getMessage(), true));
            }
        }).start();
    }

    /**
     * Get selected row from table. If no row is selected prompts user. Adds selected flight to TripSession
     */
    @FXML
    private void selectFlight() {
        FlightInfo f = resultsTable.getSelectionModel().getSelectedItem();
        if (f == null) {
            setStatus("Please select a flight from the table.", true);
            return;
        }
        TripSession s = TripSession.get();
        s.addFlight(f);

        selectedFlightLabel.setText(s.getFlights().size() + " flight added to trip. See Budget tab.");
        setStatus("Flight added to trip!", false);
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
    }
}
