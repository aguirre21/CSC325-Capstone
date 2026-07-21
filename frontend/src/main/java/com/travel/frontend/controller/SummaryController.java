package com.travel.frontend.controller;

import com.travel.frontend.model.ExpenseItem;
import com.travel.frontend.model.FlightInfo;
import com.travel.frontend.model.HotelInfo;
import com.travel.frontend.model.TripSession;
import com.travel.frontend.util.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryController {

    @FXML private VBox flightsContainer;
    @FXML private VBox hotelsContainer;
    @FXML private VBox expensesContainer;
    @FXML private Label totalCostLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        TripSession s = TripSession.get();

        flightsContainer.getChildren().clear();
        if (s.getFlights().isEmpty()) {
            flightsContainer.getChildren().add(labeled("No flights added", "summary-value"));
        } else {
            for (FlightInfo f : s.getFlights()) {
                VBox row = new VBox(2);
                row.getChildren().add(labeled(f.getAirline() + " " + f.getFlightNumber()
                        + " — " + String.format("$%.2f", f.getPriceRaw()), "summary-value"));
                row.getChildren().add(labeled(f.getOriginCode() + " → " + f.getDestinationCode(), "summary-key"));
                row.getChildren().add(labeled(formatTime(f.getDepartureRaw()) + " → " + formatTime(f.getArrivalRaw()), "summary-key"));
                flightsContainer.getChildren().add(row);
            }
        }

        hotelsContainer.getChildren().clear();
        if (s.getHotels().isEmpty()) {
            hotelsContainer.getChildren().add(labeled("No hotels added", "summary-value"));
        } else {
            for (HotelInfo h : s.getHotels()) {
                VBox row = new VBox(2);
                row.getChildren().add(labeled(h.getName() + " — " + h.getLocation(), "summary-value"));
                row.getChildren().add(labeled(h.getCheckIn() + " → " + h.getCheckOut() + " (" + h.getNights() + " nights)",
                        "summary-key"));
                row.getChildren().add(labeled(String.format("$%.2f/night × %d nights = $%.2f",
                        h.getPricePerNightRaw(), h.getNights(), h.getTotalCost()), "summary-key"));
                hotelsContainer.getChildren().add(row);
            }
        }

        expensesContainer.getChildren().clear();
        expensesContainer.getChildren().add(breakdownRow("Food & Dining", String.format("$%.2f", s.getFoodBudget())));
        expensesContainer.getChildren().add(breakdownRow("Local Transportation", String.format("$%.2f", s.getTransportBudget())));
        expensesContainer.getChildren().add(breakdownRow("Activities & Tours", String.format("$%.2f", s.getActivitiesBudget())));
        for (ExpenseItem e : s.getOtherExpenses()) {
            expensesContainer.getChildren().add(breakdownRow(e.getDescription(), String.format("$%.2f", e.getAmount())));
        }

        totalCostLabel.setText(String.format("$%.2f", s.getTotalCost()));
    }

    private javafx.scene.layout.HBox breakdownRow(String label, String value) {
        Label left = labeled(label, "summary-key");
        Label right = labeled(value, "summary-value");
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        return new javafx.scene.layout.HBox(8, left, spacer, right);
    }

    @FXML
    private void generatePdf() {
        TripSession s = TripSession.get();

        Map<String, Object> body = new HashMap<>();
        body.put("origin", s.getOrigin());
        body.put("destination", s.getDestination());
        body.put("departureDate", s.getDepartureDate());
        body.put("returnDate", s.getReturnDate());
        body.put("travelerCount", s.getTravelerCount());
        body.put("notes", s.getNotes());

        List<Map<String, Object>> flights = new ArrayList<>();
        for (FlightInfo f : s.getFlights()) {
            Map<String, Object> flightMap = new HashMap<>();
            flightMap.put("airline", f.getAirline());
            flightMap.put("flightNumber", f.getFlightNumber());
            flightMap.put("origin", f.getOriginCode());
            flightMap.put("destination", f.getDestinationCode());
            flightMap.put("departure", f.getDepartureRaw());
            flightMap.put("arrival", f.getArrivalRaw());
            flightMap.put("durationMinutes", f.getDurationMinutes());
            flightMap.put("stops", f.getStopCount());
            flightMap.put("cost", f.getPriceRaw());
            flights.add(flightMap);
        }
        body.put("flights", flights);

        List<Map<String, Object>> hotelsList = new ArrayList<>();
        for (HotelInfo h : s.getHotels()) {
            Map<String, Object> hotelMap = new HashMap<>();
            hotelMap.put("name", h.getName());
            hotelMap.put("location", h.getLocation());
            hotelMap.put("checkIn", h.getCheckIn());
            hotelMap.put("checkOut", h.getCheckOut());
            hotelMap.put("nights", h.getNights());
            hotelMap.put("pricePerNight", h.getPricePerNightRaw());
            hotelsList.add(hotelMap);
        }
        body.put("hotels", hotelsList);

        body.put("foodBudget", s.getFoodBudget());
        body.put("transportBudget", s.getTransportBudget());
        body.put("activitiesBudget", s.getActivitiesBudget());

        List<Map<String, Object>> otherExpenses = new ArrayList<>();
        for (ExpenseItem e : s.getOtherExpenses()) {
            Map<String, Object> expenseMap = new HashMap<>();
            expenseMap.put("description", e.getDescription());
            expenseMap.put("amount", e.getAmount());
            otherExpenses.add(expenseMap);
        }
        body.put("otherExpenses", otherExpenses);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Trip Summary PDF");
        chooser.setInitialFileName("trip-summary.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(statusLabel.getScene().getWindow());

        if (file == null) return;

        statusLabel.setText("Generating PDF...");
        statusLabel.setStyle("-fx-text-fill: #1565C0;");

        new Thread(() -> {
            try {
                byte[] pdf = ApiClient.postForBytes("/api/pdf/generate", body);
                Files.write(file.toPath(), pdf);
                Platform.runLater(() -> {
                    statusLabel.setText("PDF saved to " + file.getName());
                    statusLabel.setStyle("-fx-text-fill: #2E7D32;");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("PDF generation failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #C62828;");
                });
            }
        }).start();
    }

    private Label labeled(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private String formatTime(String iso) {
        if (iso == null || iso.length() < 16) return iso != null ? iso : "—";
        return iso.substring(0, 16).replace("T", " ");
    }
}
