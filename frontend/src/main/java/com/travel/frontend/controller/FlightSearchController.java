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

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
    }
}
